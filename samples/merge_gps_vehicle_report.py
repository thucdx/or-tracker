import pandas as pd
import argparse
from datetime import datetime

def merge(gps_file, vehicle_file, out):
    gps_df = pd.read_csv(gps_file)
    vehicle_df = pd.read_csv(vehicle_file)

    cols = set(gps_df.columns)
    cols2 = set(vehicle_df.columns)

    cols |= cols2

    print 'Merged cols: ', list(cols)

    merge = pd.concat([gps_df, vehicle_df], axis=0, ignore_index=True, sort=True)
    merge.sort_values(by='positionTime', inplace=True)

    merge.to_csv(out, index=False)
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Merge processed gps records and report_vehicle records." \
                                     " Sort record by order of positionTime")
    parser.add_argument("--gps", type=str, help="Path to processed gps file")
    parser.add_argument("--vehicle", type=str, help="Path to procesed report_vehicle file")
    parser.add_argument("--out", type=str, help="Path to merged file")
                        
    args = parser.parse_args()
    if args.gps != None and args.vehicle != None and args.out != None:
        gps_file = args.gps
        vehicle_file = args.vehicle
        out = args.out

        merge(gps_file, vehicle_file, out)
    else:
        parser.print_help()