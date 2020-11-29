
package RedditImgDL.PushshiftResponse;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @see <a href="http://www.jsonschema2pojo.org/">jsonschema2pojo</a>
 */
public class Response {

    @SerializedName("data")
    @Expose
    private List<Data> data = null;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

}
