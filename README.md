OnRoad Map Tracking Realtime
------
Autho `@thucdx`

Under Construction

### 1.Giới thiệu

Map Tracking Realtime sử dụng core của Barefoot. Viết lại để chạy với SpringBooot và 1 số thay đổi để phù hơp với tính năng Tracking trên OnRoad.

### 2. Import dữ liệu OSM vào DB Posgres

```
./util/postgresdb/import.sh $input $database $user $password $config $mode
```

Trong đó:
- `input`: file định dạng *.osm.pbf. Tham khảo tagging kv1 ở `../../osmdata/kv1.osm.pbf`
- `database`: tên db trong postgres. Ví dụ `kv1`
- `user`: tên user truy cập db.
- `pasword`: password truy cập db
- `config`: câu hình tốc độ mặc định trong trường hợp đường không có tagging về `maxspeed`
- `mode`: slim / normal. Mặc định slim. (Cũng chưa tìm hiểu normal khác gì slim) 


### 3. Chạy dịch vụ

#### 3.1 Cấu hình
Có thể dùng cấu hình mặc định, hoặc chỉnh ở: `src/resources/application.yml`

Trong này có 1 số nhóm cấu hình như sau:
- `server`: Chạy port nào
- `db`: thông tin truy nhập database postgres chứa thông tin về đường đã được import
- `matcher`, `tracker`, `kstate` gồm các thông số tinh chỉnh thuật toán.


#### 3.2 Khởi tao server
```
mvm spring-boot:run
```
hoặc Chạy `MapApplication.java`

Cấu hình sửa mức logging ở `src/resources/logback.xml`.

### 3.3 Tạo dữ liệu mẫu

#### Download
 Download bản tin `REPORT_GPS` (chỉ chứa mảng các GPS) và `REPORT_VEHICLE` (chứa GPS và SPEED) từ Cassandra. Lưu thành định dạng csv có header.
```
cd samples
./export_from_cassandra.sh
```
*Chú ý*: Sửa entity_id, start_time, end_time, partition, output files trong scripts

Mặc định:
  - report_gps sẽ vào `data/gps.csv`,
  - report_vehicle report sẽ vào `data/vehicle.csv`


#### Thực hiện convert 

  - 3.3.1 Convert file REPORT_GPS
```
cd samples/
python ./parse_record.py --mode gps --input $INPUT --output $OUTPUT 
```
  - 3.3.2 Convert file REPORT_VEHICLE
```
cd samples/
python ./parse_record.py --mode vehicle --input $INPUT --output $OUTPUT
```
  - 3.3.3 Merge 2 files outputs của 2 bước vừa thực hiện
```
cd samples/
python ./merge_gps_vehicle_report --gps $OUTPUT_GPS --vehicle $OUT_VEHICLE --out $MERGE_OUTPUT
```

  - 3.3.4 Sử dụng file output ($MERGE_OUTPUT) bắn lên server

```
python util/submit/or_stream.py --url localhost:1234/track --file $MREGE_OUTPUT --id sample_car_01
```

### 3.4 Chạy web monitor

- Khởi tạo web server
```
node util/monitor/monitor.js 3000 localhost 1235
```

- Truy cập web monitor ở địa chỉ: http://localhost:3000
Bật chế độ debug để xem console log`


