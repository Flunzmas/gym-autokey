def pretty_print(dictionary, max_length=30):
    """
    Convenience method to pretty print dicts.
    """
    for x in dictionary:
        r = str(dictionary[x]).replace('\n', ' ').replace('\r', '')
        r = r if len(r) < max_length else r[0:max_length] + "..."
        print("\t%s: %s" % (x, r))
    print("")
