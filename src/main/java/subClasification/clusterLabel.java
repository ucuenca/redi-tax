/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subClasification;

/**
 *
 * @author joe
 */
public class clusterLabel implements Comparable<clusterLabel> {
  private String uri;
  private int count;

  public clusterLabel(String uri, int count) {
    this.uri = uri;
    this.count = count;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
  
  @Override
    public int compareTo(clusterLabel cl) {
        return (int)(this.count - cl.getCount());
    }
  
}
