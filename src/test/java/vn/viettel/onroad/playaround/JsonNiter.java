package vn.viettel.onroad.playaround;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import vn.viettel.onroad.model.MovingSampleWrapper;

import java.util.List;

public class JsonNiter {
    public static void main(String[] args) {
        String s = "\n" +
                "{\n" +
                "    \"id\": \"sample 1\",\n" +
                "    \"arr\": [\n" +
                "        {\"lat\": 10.23232, \"lng\": 121.2323, \"heading\": 120, \"time\": 12323232},\n" +
                "        {\"lat\": 10.23233, \"lng\": 121.2322, \"heading\": 130, \"time\": 12323233},\n" +
                "        {\"lat\": 10.23234, \"lng\": 121.2324, \"heading\": 140, \"time\": 12323234},\n" +
                "        {\"lat\": 10.23235, \"lng\": 121.2325, \"heading\": 150, \"time\": 12323235}\n" +
                "    ]\n" +
                "}";

        Any a = JsonIterator.deserialize(s);

        String id = a.get("id").toString();
        System.out.println("Id" + id);

        Any arr = a.get("arr");
        Any arr2 = a.get("arr2");
        System.out.println(arr == null);


    }
}
