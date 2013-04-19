/**
 * Created with IntelliJ IDEA.
 * User: santoki
 * Date: 4/18/13
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class JsonParser {
    private static final String jsonFilePath =
            "/Users/santoki/yelp/yelp_phoenix_academic_dataset/yelp_academic_dataset_checkin.json";
    // "/Users/santoki/yelp/yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json";
    // Should replace with relative location

    public static void main(String[] args) throws Exception {

        String json_String = "";
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader(jsonFilePath));
            Gson gson = new Gson();
            //List<Yelp_Review> yr = new ArrayList<Yelp_Review>();
            List<Yelp_Checkin> yr = new ArrayList<Yelp_Checkin>();
            while ((json_String = br.readLine()) != null) {
                //Yelp_Review yp = gson.fromJson(json_String, Yelp_Review.class);
                Yelp_Checkin yp = gson.fromJson(json_String, Yelp_Checkin.class);
                yr.add(yp);

                System.out.println(yp.toString());
            }

            System.out.println("done");

			/* for (Yelp_Review result : yr) {
			 * System.out.println(result.toString()); }
			 */

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }// End Main
}// Class End
