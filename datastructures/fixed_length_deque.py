from collections import deque

class FixedLengthDeque(deque):

    max_length = 1

    def __init__(self, max_length):
        assert max_length > 0, "FixedLengthDeque max_size < 1!"
        self.max_length = max_length

    def append(self, elem):
        super(FixedLengthDeque, self).append(elem)
        if len(self) > self.max_length:
            self.popleft()