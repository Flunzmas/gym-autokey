from rewarder.dense_rewarder import DenseRewarder
from rewarder.sparse_rewarder import SparseRewarder

def create_rewarder(rewarder_type : str, r_ep_end : float,
    p_ep_end : float, p_step : float):
    '''
    A factory method producing rewarders depending on the given input params.
    '''

    if rewarder_type == 'dense': return DenseRewarder(r_ep_end, p_ep_end, p_step)
    elif rewarder_type == 'sparse': return SparseRewarder(r_ep_end, p_ep_end, p_step)
    else: return SparseRewarder(r_ep_end, p_ep_end, p_step)