/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subClasification;

import java.util.List;

/**
 *
 * @author joe
 */
public class documentCl {
  private String id;
  private String [] authors;
  private String cluster ;
  private String keywords;
  private List<String> clusterlabels;

  public List<String> getClusterlabels() {
    return clusterlabels;
  }

  public void setClusterlabels(List<String> clusterlabels) {
    this.clusterlabels = clusterlabels;
  }
   
    public documentCl(String id,  String cluster) {
    this.id = id;
    this.cluster = cluster;

  }

  public documentCl(String id,  String cluster, String [] authors, String keywords) {
    this.id = id;
    this.authors = authors;
    this.cluster = cluster;
    this.keywords = keywords;
  }
  
  

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getAuthors() {
    return authors;
  }

  public void setAuthors(String[] authors) {
    this.authors = authors;
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }
  
  
}
