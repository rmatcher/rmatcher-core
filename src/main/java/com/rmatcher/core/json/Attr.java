/**
 * Created with IntelliJ IDEA.
 * User: santoki
 * Date: 4/18/13
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */                      //
import com.google.gson.annotations.SerializedName;

public class Attr {
    @SerializedName("funny")
    public int funny;
    @SerializedName("useful")
    public int useful;
    @SerializedName("cool")
    public int cool;

    // getters and setters
    public int get_funny() {
        return funny;
    }

    public void set_funny(int funny) {
        this.funny = funny;
    }

    public int get_useful() {
        return useful;
    }

    public void set_useful(int useful) {
        this.useful = useful;
    }

    public int get_cool() {
        return cool;
    }

    public void set_cool(int cool) {
        this.cool = cool;
    }

    @Override
    public String toString() {
        return "funny: " + funny + "; " + "useful: " + useful + "; " + "cool: "
                + cool + ";\n";
    }
}