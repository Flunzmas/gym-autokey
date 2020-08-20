import socket
import json
import sys
import subprocess
import os
import signal
from time import sleep

import config as cf
from datastructures.po_anytree import parse_obligation_ast
from datastructures.ck_status_error import CKStatusError
from helpers.dict_helper import pretty_print
from helpers.connection_helper import socket_receive
from helpers.file_helper import purge_key_tmp_files, remove_key_auto_generated_files


class KeYConnector:
    """
    This class provides the functionality to control KeY while learning a tactic selector.
    """

    key_process = None
    available_tactics = []

    def __init__(self):
        """
        Sets up the connector by starting KeY and registering the exit method on atexit.
        """
        import atexit
        atexit.register(self._del)
        self.start_key()

    def _del(self):
        """
        Called on atexit: exits KeY.
        """
        self.quit_key()
        subprocess.call(cf.STOP_KEY_CMD)
        self.key_process = None
        remove_key_auto_generated_files(cf.KEY_LOG_PATH, cf.KEY_FILES_PATH)

    def retrieve_available_tactics(self):
        """
        retrieves all tactics from KeY that are currently available.
        """
        try:
            self.available_tactics = self.contact_key(["tactics"])["tactics"]
        except CKStatusError:
            self.available_tactics = []

    def load_file(self, filename):
        """
        in KeY, load the file with given file name.
        Returns a list containing the resulting obligation IDs.
        """
        abs_filename = str(cf.KEY_FILES_PATH / filename)
        try:
            return self.contact_key(["load", "filename", abs_filename])["ids"]
        except CKStatusError as ex:
            raise ex

    def get_obligation_ast(self, obligation_id):
        """in KeY, get the AST from the obligation with given ID."""
        res = parse_obligation_ast(self.contact_key(["ast", "id", obligation_id]))
        return res

    def execute_tactic(self, obligation_id, tactic):
        """
        in KeY, execute the tactic with given tactic ID on the obligation with given obligation ID.
        If the execution times out or throws an error, an obligation id of -1 is returned.
        """
        try:
            return self.contact_key(["execute", "id", obligation_id, "tactic", tactic])["ids"]
        except CKStatusError:
            return [-1]

    def cur_sequent(self, obligation_id):
        """
        Prints the sequent of the PO with given ID in KeY's representation.
        """
        response = self.contact_key(["print", "id", obligation_id])
        if response.get("response", "") == "success":
            return response["sequent"]

    def rewind_to_goal_id(self, obligation_id):
        """in KeY, rewind to the obligation with given ID."""
        return self.contact_key(["rewind", "id", obligation_id])["ids"]

    def get_open_goals(self):
        """
        returns the goals still open in KeY.
        """
        try:
            return self.contact_key(["open"])['ids']
        except CKStatusError as ckse:
            if 'IllegalStateException' in ckse.content['exception']:
                return []  # if no problem is loaded no goals are open.
            raise ckse

    # ===================================

    def start_key(self, restart=False):
        """
        Deletes tmp files from the last KeY instance (if any) and then starts KeY.
        Ensures it's running by fetching the currently available tactics.
        """
        key_log_file = open(cf.KEY_LOG_PATH, 'wb')
        key_log_file.write(b"\n Starting KeY...\n")
        self.key_process = subprocess.Popen(cf.START_KEY_SERVER_CMD.split(), stdout=key_log_file, stderr=key_log_file,
                                            preexec_fn=os.setsid)
        while len(self.available_tactics) < 1:  # retrieve the tactics as soon as KeY is available
            sleep(1)
            self.retrieve_available_tactics()

        if not restart:
            print("\nstarted KeY. Available tactics: {0}\n".format(self.available_tactics))

    def quit_key(self):
        """
        Quits KeY by sending a SIGKILL signal.
        """
        if self.key_process:
            pid = self.key_process.pid
            os.killpg(pid, signal.SIGKILL)
            self.available_tactics = []
        purge_key_tmp_files(cf.KEY_LOG_PATH, cf.KEY_TMP_FILE_PATH,
                            cf.KEY_TMP_FILE_REGEX, cf.GRADLE_DAEMON_CACHE_DIR)

    def restart_key(self):
        """
        Quits and then restarts KeY.
        """
        self.quit_key()
        self.start_key(restart=True)

    # ===================================

    def contact_key(self, arguments):
        """
        The method interfacing KeY's functionality, taking commands and their parameters as arguments.
        """
        if len(arguments) < 1:
            print("At least command argument needed")
            exit(1)
        if len(arguments) % 2 == 0:
            print("Need an odd number of arguments")
            exit(2)

        # assemble the command
        command = {"command": arguments.pop(0)}
        while len(arguments) > 0:
            k = arguments.pop(0)
            v = arguments.pop(0)
            command[k] = v
        json_command = json.dumps(command)

        # communicate with KeY
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            result_data = ""
            try:
                s.settimeout(cf.KEY_TACTIC_TIME_LIMIT)
                s.connect((cf.KEY_SERVER_ADDRESS, cf.KEY_SERVER_PORT))
                s.sendall(json_command.encode('utf-8'))
                s.shutdown(1)
                result_data = socket_receive(s).decode("utf-8")

            except (socket.timeout, TimeoutError):
                result_data = b"{\"exception\": \"timeout\"}"
            except (ConnectionRefusedError, ConnectionResetError):
                result_data = b"{\"exception\": \"bad connection\"}"

        result_dict = json.loads(result_data)
        # print("response:")
        # pretty_print(result_dict)
        if "exception" in result_dict:
            raise CKStatusError(result_dict)
        return result_dict


# ===================================

if __name__ == "__main__":
    sys.argv.pop(0)
    kc = KeYConnector()
    kc.contact_key(sys.argv)
