class CKStatusError(Exception):
    """Custom error for bad ContactKey socket responses."""

    def __init__(self, response_content):
        self.content = response_content
        # for key in responseContent:
        #    print(str(key) + ": " + str(responseContent.get(key)))