/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package corticalClasification;

import java.util.Comparator;
import org.openrdf.model.URI;

/**
 *
 * @author joe
 */
public class AreaUnesco implements Comparator<AreaUnesco> {

    private String label;
    private URI uri;
    private Double Score;

    public AreaUnesco() {
    }

    public AreaUnesco(String label, URI uri, Double Score) {
        this.label = label;
        this.uri = uri;
        this.Score = Score;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Double getScore() {
        return Score;
    }

    public void setScore(Double Score) {
        this.Score = Score;
    }

    @Override
    public String toString() {
        return "[uri: " + uri + ", label: " + label + "]";
    }

    Comparator<AreaUnesco> comparator = new Comparator<AreaUnesco>() {

        @Override
        public int compare(AreaUnesco o1, AreaUnesco o2) {
            int res = o1.getScore().compareTo(o2.getScore());
            if (res == 0) {
                return o1.getScore() < o2.getScore() ? -1 : 1;
            } else {
                return res;
            }
        }

    };

    @Override
    public int compare(AreaUnesco o1, AreaUnesco o2) {
        int res = o1.getScore().compareTo(o2.getScore());
        if (res == 0) {
            return o1.getScore() < o2.getScore() ? -1 : 1;
        } else {
            return res;
        }
    }

}
