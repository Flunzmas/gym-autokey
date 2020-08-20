import os
import re
import shutil

def remove_key_auto_generated_files(key_log_path : str, key_files_path : str):
    '''
    Recursively searches for all automatically generated .proof files in the
    examples folder and deletes them. They would litter the directories otherwise.
    '''
    key_log_file = open(key_log_path, 'wb')
    key_log_file.write(b"\n Deleting KeY auto-generated files...\n")   
    for dir_name, subdir_list, file_list in os.walk(key_files_path):
        for fname in file_list:
            if re.search(r'.*auto(\.[0-9]+)?\.proof$', fname):
                os.remove(os.path.join(dir_name, fname))

def purge_key_tmp_files(key_log_path : str, key_tmp_file_path : str,
    key_tmp_file_regex: str, gradle_daemon_cache_dir : str):
    '''
    Recursively walks the path for temp. files and deletes all files used/generated
    by the previous KeY instance. Also deletes all daemon files.
    '''
    key_log_file = open(key_log_path, 'wb')
    key_log_file.write(b"\n Deleting KeY tmp files...\n")
    for _, dirs, files in os.walk(key_tmp_file_path):
        for t_dir in dirs:
            if re.match(key_tmp_file_regex, t_dir):
                shutil.rmtree(key_tmp_file_path + t_dir, ignore_errors=True)
        for t_file in files:
            if re.match(key_tmp_file_regex, t_file) and os.path.exists(key_tmp_file_path + t_file):
                os.remove(key_tmp_file_path + t_file)
    for _, dirs, files in os.walk(gradle_daemon_cache_dir):
        for t_dir in dirs:
            shutil.rmtree(gradle_daemon_cache_dir + '/' + t_dir, ignore_errors=True)