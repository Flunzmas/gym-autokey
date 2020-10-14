import gym_autokey.envs.obs_extractor.text_obs_extractor as toe

class NameOnlyTextObsExtractor(toe.TextObsExtractor):

    def obs_from_anytree(self, cur_node):
        """
        Creates a text string containing the AST information, but without the op class.
        """
        cur_text = cur_node.op_name

        c_count = len(cur_node.children)
        if c_count > 0:
            cur_text += "("

            for i in range(c_count):
                cur_text += self.obs_from_anytree(cur_node.children[i])
                if i < c_count - 1:
                    cur_text += ", "
            cur_text += ")"

        return cur_text