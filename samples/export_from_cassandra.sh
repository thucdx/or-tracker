#!/bin/bash

entity_id=5a8848e0-801d-11eb-8419-03264d3b8aac
start_time=1623369600000
end_time=1623373200000
partition=1622505600000

output_vehicle=data/vehicle.csv
output_gps=data/gps.csv

# REPORT VEHICLE
REPORT_VEHICLE_QUERY="
PAGING OFF;

SELECT entity_id, key, partition, ts, toDate(ts), long_v 
FROM thingsboard.ts_kv_cf 
WHERE partition = $partition 
  AND entity_type='ASSET' 
AND entity_id = $entity_id 
  AND key IN ('publishData.dataFlow.gpsPosition.heading', 
            'publishData.dataFlow.gpsPosition.lat',  
             'publishData.dataFlow.gpsPosition.lng', 
             'publishData.dataFlow.gpsPosition.positionTime', 
             'publishData.dataFlow.gpsPosition.height', 
             'publishData.dataFlow.speed')
and ts >= $start_time and ts <= $end_time
ALLOW FILTERING;
"

echo $REPORT_VEHICLE_QUERY > tmp_query.cql

cqlsh -f tmp_query.cql | tee .tmp_output

sed -e 's/\ //g;' -e  '/^----.*/d; /^(/d; /^\s*$/d; /^Disable.*/d;' -e 's/|/\,/g;' .tmp_output | tee $output_vehicle

rm -rf tmp_query.cql
rm -rf .tmp_output

echo "Save vehicle records to $output_vehicle"

# REPORT GPS
REPORT_GPS_QUERY="
PAGING OFF;

SELECT entity_id, key, partition, ts, toDate(ts), long_v
from thingsboard.ts_kv_cf
where
      entity_type = 'ASSET'
      and entity_id=$entity_id
  and key IN  ('publishData.gps[0].lat', 'publishData.gps[0].lng','publishData.gps[0].positionTime','publishData.gps[0].heading',
              'publishData.gps[1].lat', 'publishData.gps[1].lng', 'publishData.gps[1].positionTime','publishData.gps[1].heading',
              'publishData.gps[2].lat', 'publishData.gps[2].lng','publishData.gps[2].positionTime','publishData.gps[2].heading',
              'publishData.gps[3].lat', 'publishData.gps[3].lng','publishData.gps[3].positionTime','publishData.gps[3].heading',
              'publishData.gps[4].lat', 'publishData.gps[4].lng', 'publishData.gps[4].positionTime','publishData.gps[4].heading',
              'publishData.gps[5].lat', 'publishData.gps[5].lng','publishData.gps[5].positionTime','publishData.gps[5].heading',
              'publishData.gps[6].lat', 'publishData.gps[6].lng','publishData.gps[6].positionTime','publishData.gps[6].heading',
              'publishData.gps[7].lat', 'publishData.gps[7].lng','publishData.gps[7].positionTime','publishData.gps[7].heading',
              'publishData.gps[8].lat', 'publishData.gps[8].lng','publishData.gps[8].positionTime','publishData.gps[8].heading')
    and partition = $partition
and ts >= $start_time and ts <= $end_time
ALLOW FILTERING;
"

echo $REPORT_GPS_QUERY > tmp_query.cql

cqlsh -f tmp_query.cql | tee .tmp_output

sed -e 's/\ //g;' -e  '/^----.*/d; /^(/d; /^\s*$/d; /^Disable.*/d;' -e 's/|/\,/g;' .tmp_output | tee $output_gps

echo "Saved GPS record to $output_gps"

rm -rf tmp_query.cql
rm -rf .tmp_output

