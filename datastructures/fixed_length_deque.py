from collections import deque

class FixedLengthDeque(deque):
    '''
    A deque initialized with a fixed size. When a new item is added
    to a full deque, the oldest item is removed.
    '''

    size = 1

    def __init__(self, size):
        assert size > 0, "tried to initialize FixedLengthDeque with size < 1!"
        self.size = size

    def append(self, elem):
        super(FixedLengthDeque, self).append(elem)
        if len(self) > self.size:
            self.popleft()