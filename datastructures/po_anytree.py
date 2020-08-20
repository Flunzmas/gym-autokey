import json

from anytree import Node, RenderTree
from apted.helpers import Tree

node_id = 0

def parse_obligation_ast(content):
    """
    Brings the AST into an 'anytree' shape using recursion.
    """
    global node_id
    node_id = 0
    po_id = str(content.get('id', -1))
    ast_anytree_root_node = Node(po_id, op_name="RootImp", op_class="RootImp", id=node_id, depth=0)
    node_id += 1
    parse_second_level_ast(content, ast_anytree_root_node, "antecedent", "and", 1)
    parse_second_level_ast(content, ast_anytree_root_node, "succedent", "or", 1)
    return ast_anytree_root_node

def parse_second_level_ast(content, root_node, key, junctor_name, depth):
    """
    This method is needed because the succedent and antecedent nodes of a goal AST
    each hold a list of subtrees joined by a logical conjunction/disjunction.
    """
    global node_id
    subtree = content[key]
    if len(subtree) == 1:
        _parse_obligation_ast_recursive(content[key][0], root_node, depth)
    elif len(subtree) > 1:
        junctor_node = Node(junctor_name, op_name=junctor_name, op_class="Junctor", id=node_id, depth=depth, parent=root_node)
        node_id += 1
        for cn in content[key]:
            _parse_obligation_ast_recursive(cn, junctor_node, depth+1)   

def _parse_obligation_ast_recursive(content, parent_node, depth):
    """
    Recursively adds children to given parent node until there is no more content.
    """
    global node_id
    op_n = content["op"]
    op_c = content["op_class"].split(".")[-1] # underscore because given that way by the interface
    cur_node = Node(op_n, op_name=op_n, op_class=op_c, id=node_id, depth=depth, parent=parent_node)
    node_id += 1
    children = content["children"]
    for child_content in children:
        _parse_obligation_ast_recursive(child_content, cur_node, depth+1)

def print_anytree(root_node):
    """
    Pretty-prints the given anytree.
    """
    for pre, _, node in RenderTree(root_node):
        print('{pre} id = {id} | d = {d} | {n} ({c})'.format(pre=pre, id=node.id, d=node.depth, n=node.op_name, c=node.op_class))
    print("")

def ast_anytree_to_json(root_node):
    """
    Serializes an anytree to json format and returns it.
    """
    from anytree.exporter import JsonExporter
    exporter = JsonExporter(indent=2, sort_keys=True)
    return exporter.export(root_node)

def ast_json_to_anytree(json_data):
    """
    Deserializes an anytree in json format and returns its root node.
    """
    from anytree.importer import JsonImporter
    importer = JsonImporter()
    return importer.import_(json_data)

def validate_anytree(root_node):
    """ 
    Returns true only if given tree corresponds to the PO anytree syntax.
    """
    return True
    raise NotImplementedError

def ast_anytree_to_apted(cur_node):
    """
    Converts an Anytree to an equivalent APTED Tree.
    """
    children_apted_nodes = []
    for child in list(cur_node.children):
        children_apted_nodes.append(ast_anytree_to_apted(child))
    return Tree(cur_node.op_name + " " + cur_node.op_class, *children_apted_nodes)

def ast_anytree_to_node_list(cur_node):
    """
    Converts an Anytree to an equivalent APTED Tree.
    """
    node_list = [cur_node]
    for child in list(cur_node.children):
        node_list.extend(ast_anytree_to_node_list(child))
    return node_list