class CKStatusError(Exception):
    """Custom error type for bad socket responses when communicating with KeY."""

    def __init__(self, response_content):
        self.content = response_content