__author__ = "thucdx"
__copyright__ = "Copyright 2021 Viettel"

import datetime
import optparse
import pandas as pd
import json
import subprocess
import time
import datetime
import requests

parser = optparse.OptionParser("or_stream.py [options]")
parser.add_option("--host", dest="host", help="IP address of tracker.")
parser.add_option("--port", dest="port", help="Port of tracker.")
parser.add_option("--file", dest="file", help="CSV file with sample data.")
parser.add_option("--id", dest="id", help="Object id.")
parser.add_option("--step", action="store_true", dest="step", default=False, help="Send stepwise.")

(options, args) = parser.parse_args()

if options.file is None or options.host is None or options.port is None:
    parser.print_help()
    exit(1)
