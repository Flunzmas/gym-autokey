class Rewarder:
    """
    Base class for all rewarders.
    While each of them implement their own rewarding system, determining the
    success status of a parent goal given its children is common for every
    rewarding type as for a successful proof, everything has to be closed.
    """

    @staticmethod
    def calculate_success_status(child_statuses):
        """
        Returns 'success' if the the status of all children is 'success'.
        Returns 'crash' if a child crashed.
        Otherwise: returns 'fail'.
        """

        status = "fail"
        if child_statuses == {'success'}:
            status = "success"
        elif "crash" in child_statuses:
            status = "crash"
        return status

    def end_and_reward_subepisode(self, subepisode):
        """
        Implemented by the subclasses.
        """
        raise NotImplementedError
