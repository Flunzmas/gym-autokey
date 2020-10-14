from anytree import Node

from gym_autokey.envs.datastructures.ck_status_error import CKStatusError


class PONode(Node):
    """
    A subclass of an anytree node which holds id, ast, features and tactic executed on this
    node (if present).
    """

    def __init__(
            self, name, id, ast, features, tactic=None, reward=0, origin=None, parent=None, children=None, **kwargs):
        self.id = id
        self.ast = ast
        self.features = features
        self.tactic = tactic
        self.reward = reward
        self.origin = origin
        super(PONode, self).__init__(name, parent=parent, children=children, **kwargs)

    @classmethod
    def from_id(cls, po_id, kc, fh, parent=None):
        """
        Creates a new PONode from given id by retrieving all necessary information.
        If retrieving the information fails, creates a bad node that is to be detected by the users.
        """
        try:
            ast = kc.get_goal_ast(po_id)
            features = fh.obs_from_anytree(ast)
        except CKStatusError:
            return PONode("bad node", id=-1, ast=None, features=None, parent=parent)
        return PONode(str(po_id), id=po_id, ast=ast, features=features, parent=parent)
