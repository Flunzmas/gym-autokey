class Experience(object):
    '''
    An experience is the result of an environment step, encapsulating a goal's
    id, its features, the action taken, the reward and the new id and features.
    '''

    def __init__(self, id : str = "XXXX", obs=None, action="NONE", reward=None, new_id="XXXX", new_obs=None):

        self.id = id
        self.obs = obs
        self.action = action
        self.reward = reward
        self.new_id = new_id
        self.new_obs = new_obs

    def to_compact_string(self):
        '''
        Returns a compect string without the exact features.
        '''
        return ("({id}) --|{ac}|--[{rew}]--> ({nid})".format\
        (id=self.id, ac=self.action, rew=self.reward, nid=self.new_id))