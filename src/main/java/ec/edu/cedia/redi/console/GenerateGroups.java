/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package corticalClasification;
import com.google.common.base.Joiner;
import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.RediRepository;
import ec.edu.cedia.redi.unesco.UnescoNomeclature;
import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.URI;
import plublication.Preprocessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe
 */
public class GenerateGroups {

    /**
     * @param args the command line arguments
     */
    private static final Logger log = LoggerFactory.getLogger(GenerateGroups.class);
    
   /* public static void main(String[] args) {
    
            
            
    }*/
    
    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




/**
 *
 * @author joe
 */


    private static List<AreaUnesco> filterAreas(List<AreaUnesco> l, int n, double porcentage) {

        Collections.sort(l, new AreaUnesco().reversed());

        if (n >= l.size()) {
            return l;
        }

        Double min = 0.0;
        for (int i = 0; i < l.size(); i++) {

            if (i == 0) {
                min = l.get(i).getScore() - l.get(i).getScore() * porcentage;
            } else if (l.get(i).getScore() < min || n < i + 1) {
                return l.subList(0, i);
            }

        }

        return l;
    }
/*
    private static void registerAuthorAreas(URI uri, List<AreaUnesco> filterAreas) {
        System.out.println("Resultado Final: " + uri);
        for (AreaUnesco a : filterAreas) {
            System.out.print("|" + a.getLabel() + "-" + a.getScore());
        }
        ///throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    /**
     * @param args the command line arguments
     */
    private Preprocessing processing = Preprocessing.getInstance();
    private static Preprocessing p = Preprocessing.getInstance();

    public static void main(String[] args) throws Exception {

        UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();
        UnescoNomeclature unesco = new UnescoNomeclature(conn);
        List<URI> listUris = unesco.twoDigitResources();

        Preprocessing p = Preprocessing.getInstance();
        RediRepository rp = RediRepository.getInstance();
        
        Redi r = new Redi(rp);
        Author actual = null;
        List<Author> authors = r.getAuthors();
        Map<String, String[]> myHashMap = new HashMap<>();
        Map<String, List<AreaUnesco>> authorAreas = new HashMap<>();
        /* String[] e = {"val1.1", "val1.2"};
 
        myHashMap.put("valor1", neAuthor actualw String[]{"val1","val2"} );
        myHashMap.put("valor3", new String[]{"val1.1","val2.1"});*/

        File f = new File();

//        for (Author a : authors) {
//            System.out.println(a.getURI()+" "+r.isAuthorInCluster(a.getURI()));
//        }
        for (Author a : authors) {
            actual = a;
            System.out.println("AUTOR: " + actual.getURI());

            if (r.isAuthorInCluster(a.getURI())) {
                System.out.println(actual.getURI() + " Procesado");
                continue;
            }

            List<AreaUnesco> areasList = new ArrayList();
            // continue;

            // System.out.print (p.CompareText("Computer applications , Archive automation, Artificial intelligence, Expert systems,  Pattern recognition, Robotics",
            //        "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration"));
            // String userKeywords = "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration";
            String userKeywords = actual.getKeywords();

            String bestCategory = "";
            URI bestCategoryURI = null;
            Double bestScore = 0.0;
            for (URI l : listUris) {
                String arrayLabels = "";
                System.out.println("*********");
                String label2 = unesco.label(l, "en").getLabel();
                System.out.println(label2);
                List<URI> listN = unesco.narrow(l);
                for (URI nl : listN) {
                    String label4 = unesco.label(nl, "en").getLabel();
                    System.out.println(label4);
                    // arrayLabels = arrayLabels + ", " + label4;
                    String arrayLabels4 = label4;
                    if (!label4.contains("Other")) {
                        //  if (listN.size() < 30) {
                        List<URI> listNl = unesco.narrow(nl);
                        int count = 0;
                        for (URI nnl : listNl) {
                            String label6 = unesco.label(nnl, "en").getLabel();
                            if (!label6.contains("Other")) {
                                //   System.out.println(label6);
                                // arrayLabels = arrayLabels + ", " + label6.replace(".", "");
                                arrayLabels4 = arrayLabels4 + ", " + StringUtils.stripAccents(label6);
                                count++;

                                //     }
                            }
                        }

                        Double val;
                        Object number = p.CompareText(arrayLabels4, StringUtils.stripAccents(userKeywords), "weightedScoring");
                        if (number instanceof Double) {
                            val = (Double) number;
                        } else {
                            val = ((BigDecimal) number).doubleValue();
                        }

                        AreaUnesco area = new AreaUnesco(label4, nl, val);
                        areasList.add(area);

                        if (bestScore.doubleValue() < val.doubleValue()) {
                            bestScore = val;
                            bestCategory = label4;
                            bestCategoryURI = nl;
                        }
                        System.out.println(actual.getURI() + " Score : " + label4 + "-" + val);

                    }
                }
                //   Double val = (Double) p.CompareText(arrayLabels, userKeywords, "weightedScoring");

            }

            //System.out.println("La mejor categoria para "+actual.getURI().toString()+" es :" + bestCategory + bestScore);
            //authorAreas.put (actual.getURI().toString() , areasList);
            // myHashMap.put (actual.getURI().toString() ,new String [] {bestCategory,bestCategoryURI.toString()});
            //   f.writeMapCSV(myHashMap);  
            r.store(actual.getURI(), filterAreas(areasList, 2, 0.1));
//         registerAuthorAreas (  actual.getURI() , filterAreas ( areasList , 2 , 0.1)); 

        }
        //f.writeMapCSV(myHashMap);

        conn.close();
    }

//        UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();
//        UnescoNomeclature unesco = new UnescoNomeclature(conn);
//
//        String userKeywords = "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration";
//        String bestCategory = "";
//        Double bestScore = 0.0;
//        for (URI l : unesco.twoDigitResources()) {
//            String arrayLabels = "";
//            String label = unesco.label(l, "en").getLabel();
//            System.out.println(label);
//            List<URI> listN = unesco.narrow(l);
//            for (URI nl : listN) {
//                String label4 = unesco.label(nl, "en").getLabel();
//                System.out.println(label4);
//                arrayLabels = arrayLabels + ", " + label4;
//                if (listN.size() < 50) {
//                    List<URI> listNl = unesco.narrow(nl);
//                    for (URI nnl : listNl) {
//                        String label6 = unesco.label(nnl, "en").getLabel();
//                        if (!label6.contains("Other")) {
//                            System.out.println(label6);
//                            arrayLabels = arrayLabels + ", " + label6.replace(".", "");
//                        }
//                    }
//                }
//            }
//            Double val = (Double) p.CompareText(arrayLabels, userKeywords);
//            if (bestScore > val) {
//                bestScore = val;
//                bestCategory = label;
//            }
//            System.out.println("Score : " + label + "-" + val);
//        }
//
//        System.out.print("La mejor categoria es :" + bestCategory);
//        conn.close();
//        String txt = "[{\"text\":\"6111 aluminium alloy;active site;adhesion;adverse effect;analytical chemistry;aqueous solution;atomic force microscopy;calcium;ceramic materials;ceramic matrix composite;cerium;cerium iv oxide cerium iii oxide cycle;composite material;conversion coating;corrosion;chemistry;chromatography;dielectric spectroscopy;dysprosium;electrical impedance;electrospinning;environmental chemistry;environmental impact statement;erosion corrosion;forensic engineering;heat treating;inorganic chemistry;intergranular corrosion;iron;lanthanide;laser;linear polarization;magnesium;manganese;materials science;metal;metallurgy;microstructure;mineralogy;morphology;nanocomposite;nanofiber;nanoparticle;nanotechnology;nickel;nitride;nitrogen;nuclear chemistry;nuclear magnetic resonance;nuclear physics;nuclear reactor;optical microscope;organic chemistry;particle size;performance;phosphorus;physical chemistry;physics;polarization;polymer;polymer chemistry;radiochemistry;residual stress;room temperature;scanning electron microscope;smart material;sol gel;surface modification;thin film;tissue engineering;titanium oxide;uranium;visual inspection;wear;x ray crystallography;x ray photoelectron spectroscopy\"},{\"text\":\"Science space (2104 2102 3324);Soil Science (Soil Science) ;Oceanography;Meteorology ;Hydrology ;Geophysics;Geology;Geography ;Geodesy;Geochemistry;Climatology ;Atmospheric sciences ;Space physiology ;Space medicine;Exobiology;Soil physics;Soil morphology and genesis;Soil mineralogy;Soil microbiology;Soil mechanics (agriculture);Soil engineering;Soil xonservation;Soil classification;Soil chemistry;soil cartography;Soil biology;Soil biochemistry;Marine aquaculture (oceanography);Renewable resources (oceanography);Marine Geology (oceanography);Underwater sounds;Shore and near-shore processes;Sea ice ;Sea-air interactions ;Physical oceanography ;Ocean-bottom processes ;Marine zoology ;Marine botany ;Descriptive oceanography;Chemical oceanography;Biological oceanography;Weather modification;Weather analysis;Tropical meteorology;Synoptic meteorology;Satellite meteorology ;Rocket meteorology;Radio meteorology;Radar meteorology;Polar meteorology;Operational forecasting (weather);Observation briefing (weather);Numerical weather prediction;Micrometeorology;Mesometeorología;Marine meteorology ;Industrial meteorology;Hydrometeorology ;Extended weather forecasting;Air pollution ;Agricultural mMeteorology;Transpiration;Surface waters;Soil moisture;Snow;Quality of water ;Precipitation;Permafrost;Limnology;Ice ;Hydrography;Hydrobiology;Groundwater;Glaciology ;Evaporation;Erosion (water);Tectonics;Solid-earth and geophysics;Seismology and seismic exploration;Paleomagnetism;Heat flow (earth);Gravity (earth) and gravity exploration ;Geomagnetism and magnetic explotarion;Well log analysis;Volcanology;Structural geology;Stratigraphy ;Sedimentology;Rock mechanisms;Remote sensing (geology);Photogeology;Petrology, sedimentary;Petrology, igneous and metamorphic;Petroleum geology ;Mineralogy;Mineral deposits;Glacial geology ;Geothermal processes and energy ;Geomorphology;Geological surveys;Geohydrology ;Environmental geology;Engineering geology;Coal geologi ;Areal geology;Topographic geography;Physical geography;Medical geography;Location theory;Land utilization ;Geography of natural resources;Geographical cartography;Biogeography ;Theoretical geodesy;Satellite geodesy ;Physical geodesy;Geodetic surveying;Geodesic photogrammetry;Geodesic navigation;Geodesic cartography;Geodesic astronomy ;Trace elements distribution;Stable isotopes;Organic geochemistry;Low temperatures geochemistry;High temperature geochemistry;Geochronology and radio isotope;Exploration geochemistry;Experimental petrology;Cosmochemistry ;Regional climatology;Physical climatology;Paleoclimatology;Microclimatology;Bioclimatology;Applied climatology;Analytical climatology;Solar wind;Radioactive transfer;Precipitation physics;Numerical modelling;Magnetospheric waves;Magnetospheric particles;Ionosphere;Geomagnetic pulsations;Dissemination (atmospheric);Cosmic rays ;Cloud physics;Aurora;Atmospheric turbulence\"}]";
//        String txt1 = "6111 aluminium alloy;active site;adhesion;adverse effect;analytical chemistry;aqueous solution;atomic force microscopy;calcium;ceramic materials;ceramic matrix composite;cerium;cerium iv oxide cerium iii oxide cycle;composite material;conversion coating;corrosion;chemistry;chromatography;dielectric spectroscopy;dysprosium;electrical impedance;electrospinning;environmental chemistry;environmental impact statement;erosion corrosion;forensic engineering;heat treating;inorganic chemistry;intergranular corrosion;iron;lanthanide;laser;linear polarization;magnesium;manganese;materials science;metal;metallurgy;microstructure;mineralogy;morphology;nanocomposite;nanofiber;nanoparticle;nanotechnology;nickel;nitride;nitrogen;nuclear chemistry;nuclear magnetic resonance;nuclear physics;nuclear reactor;optical microscope;organic chemistry;particle size;performance;phosphorus;physical chemistry;physics;polarization;polymer;polymer chemistry;radiochemistry;residual stress;room temperature;scanning electron microscope;smart material;sol gel;surface modification;thin film;tissue engineering;titanium oxide;uranium;visual inspection;wear;x ray crystallography;x ray photoelectron spectroscopy";
//        String txt2 = "Science space (2104 2102 3324);Soil Science (Soil Science) ;Oceanography;Meteorology ;Hydrology ;Geophysics;Geology;Geography ;Geodesy;Geochemistry;Climatology ;Atmospheric sciences ;Space physiology ;Space medicine;Exobiology;Soil physics;Soil morphology and genesis;Soil mineralogy;Soil microbiology;Soil mechanics (agriculture);Soil engineering;Soil xonservation;Soil classification;Soil chemistry;soil cartography;Soil biology;Soil biochemistry;Marine aquaculture (oceanography);Renewable resources (oceanography);Marine Geology (oceanography);Underwater sounds;Shore and near-shore processes;Sea ice ;Sea-air interactions ;Physical oceanography ;Ocean-bottom processes ;Marine zoology ;Marine botany ;Descriptive oceanography;Chemical oceanography;Biological oceanography;Weather modification;Weather analysis;Tropical meteorology;Synoptic meteorology;Satellite meteorology ;Rocket meteorology;Radio meteorology;Radar meteorology;Polar meteorology;Operational forecasting (weather);Observation briefing (weather);Numerical weather prediction;Micrometeorology;Mesometeorología;Marine meteorology ;Industrial meteorology;Hydrometeorology ;Extended weather forecasting;Air pollution ;Agricultural mMeteorology;Transpiration;Surface waters;Soil moisture;Snow;Quality of water ;Precipitation;Permafrost;Limnology;Ice ;Hydrography;Hydrobiology;Groundwater;Glaciology ;Evaporation;Erosion (water);Tectonics;Solid-earth and geophysics;Seismology and seismic exploration;Paleomagnetism;Heat flow (earth);Gravity (earth) and gravity exploration ;Geomagnetism and magnetic explotarion;Well log analysis;Volcanology;Structural geology;Stratigraphy ;Sedimentology;Rock mechanisms;Remote sensing (geology);Photogeology;Petrology, sedimentary;Petrology, igneous and metamorphic;Petroleum geology ;Mineralogy;Mineral deposits;Glacial geology ;Geothermal processes and energy ;Geomorphology;Geological surveys;Geohydrology ;Environmental geology;Engineering geology;Coal geologi ;Areal geology;Topographic geography;Physical geography;Medical geography;Location theory;Land utilization ;Geography of natural resources;Geographical cartography;Biogeography ;Theoretical geodesy;Satellite geodesy ;Physical geodesy;Geodetic surveying;Geodesic photogrammetry;Geodesic navigation;Geodesic cartography;Geodesic astronomy ;Trace elements distribution;Stable isotopes;Organic geochemistry;Low temperatures geochemistry;High temperature geochemistry;Geochronology and radio isotope;Exploration geochemistry;Experimental petrology;Cosmochemistry ;Regional climatology;Physical climatology;Paleoclimatology;Microclimatology;Bioclimatology;Applied climatology;Analytical climatology;Solar wind;Radioactive transfer;Precipitation physics;Numerical modelling;Magnetospheric waves;Magnetospheric particles;Ionosphere;Geomagnetic pulsations;Dissemination (atmospheric);Cosmic rays ;Cloud physics;Aurora;Atmospheric turbulence";
//        getEntities(txt);
//        p.CompareText(StringUtils.stripAccents(txt1), StringUtils.stripAccents(txt2));
    /*       corticalTest c = new corticalTest();
     List<Author> authors = c.getAuthors();
     Map<URI, String> unesco = c.getWords(150);
     Map<URI, Map<URI, Double>> result = c.getWeights(unesco, authors);
     for (Map.Entry<URI, Map<URI, Double>> r : result.entrySet()) {
     System.out.println(r.getKey());
     for (Map.Entry<URI, Double> score : r.getValue().entrySet()) {
     System.out.println("\t\t" + score.getKey() + "\t\t" + score.getValue());
     }
     }
     }*/
    private static final HttpClient httpclient = HttpClients.createDefault();

    public static void getEntities(String json) {
        try {
            URIBuilder builder = new URIBuilder("http://api.cortical.io/rest/compare");
            builder.setParameter("retina_name", "en_associative");
            builder.setParameter("api-key", "1c556a80-8595-11e6-a057-97f4c970893c");
            builder.setParameter("Content-Type", "application/json");

            java.net.URI uri = builder.build();

            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("Accept", "application/json");
            StringEntity textEntity = new StringEntity(json);
            httpPost.setEntity(textEntity);
            // Request response
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                System.out.println(result);
//                ObjectNode root = (ObjectNode) mapper.readTree(result);
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (IOException | ParseException | URISyntaxException ex) {
        }
    }

    public Map<URI, Map<URI, Double>> getWeights(Map<URI, String> taxonomy, List<Author> authors) throws IOException, InterruptedException {
        Map<URI, Map<URI, Double>> authorScores = new HashMap<>();
        for (Author author : authors) {
            Map<URI, Double> scores = new HashMap<>();
            for (Map.Entry<URI, String> tax : taxonomy.entrySet()) {
                String keywords = StringUtils.stripAccents(author.getKeywords());
                String unescoWords = StringUtils.stripAccents(tax.getValue());
                int wordsAuthor = keywords.split(";").length;
                int wordsUnesco = unescoWords.split(";").length;
                System.out.println("[Unesco:" + wordsUnesco + ", Author:" + wordsAuthor + "]");
                double score = (double) processing.CompareText(keywords, unescoWords, "");
                scores.put(tax.getKey(), score);
            }
            authorScores.put(author.getURI(), scores);
        }
        return authorScores;
    }

    public Map<URI, String> getWords(int limit) {
        Map<URI, String> unescoTax = new HashMap<>();
        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
            UnescoNomeclature unesco = new UnescoNomeclature(conn);
            for (URI twoDigitResource : unesco.twoDigitResources()) {
                List<URI> fourDigitResources = unesco.narrow(twoDigitResource).stream()
                        .filter(uri -> !uri.stringValue().contains("99"))
                        .collect(Collectors.toList());
                List<URI> sixDigitResources = fourDigitResources.stream()
                        .map(fourDigit -> unesco.narrow(fourDigit))
                        .flatMap(List::stream)
                        .filter(uri -> !uri.stringValue().contains("99"))
                        .collect(Collectors.toList());

                List<URI> all = new ArrayList<>();
                all.addAll(fourDigitResources);
                all.addAll(sixDigitResources);
                List<String> words;

                if (limit == -1) {
                    words = all.stream().map(uri -> unesco.label(uri, "en").getLabel())
                            .collect(Collectors.toList());
                } else {
                    words = all.stream().map(uri -> unesco.label(uri, "en").getLabel())
                            .limit(limit).collect(Collectors.toList());
                }
                unescoTax.put(twoDigitResource, Joiner.on(";").join(words));
            }
        } catch (Exception ex) {
        }
        return unescoTax;
    }

    public List<Author> getAuthors() {
        try (RediRepository r = RediRepository.getInstance()) {
            Redi redi = new Redi(r);
            return redi.getAuthors();
        } catch (Exception ex) {
        }
        return Collections.emptyList();
    }

//        UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();
//        UnescoNomeclature unesco = new UnescoNomeclature(conn);
//
//        String userKeywords = "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration";
//        String bestCategory = "";
//        Double bestScore = 0.0;
//        for (URI l : unesco.twoDigitResources()) {
//            String arrayLabels = "";
//            String label = unesco.label(l, "en").getLabel();
//            System.out.println(label);
//            List<URI> listN = unesco.narrow(l);
//            for (URI nl : listN) {
//                String label4 = unesco.label(nl, "en").getLabel();
//                System.out.println(label4);
//                arrayLabels = arrayLabels + ", " + label4;
//                if (listN.size() < 50) {
//                    List<URI> listNl = unesco.narrow(nl);
//                    for (URI nnl : listNl) {
//                        String label6 = unesco.label(nnl, "en").getLabel();
//                        if (!label6.contains("Other")) {
//                            System.out.println(label6);
//                            arrayLabels = arrayLabels + ", " + label6.replace(".", "");
//                        }
//                    }
//                }
//            }
//            Double val = (Double) p.CompareText(arrayLabels, userKeywords);
//            if (bestScore > val) {
//                bestScore = val;
//                bestCategory = label;
//            }
//            System.out.println("Score : " + label + "-" + val);
//        }
//
//        System.out.print("La mejor categoria es :" + bestCategory);
//        conn.close();
//        String txt = "[{\"text\":\"6111 aluminium alloy;active site;adhesion;adverse effect;analytical chemistry;aqueous solution;atomic force microscopy;calcium;ceramic materials;ceramic matrix composite;cerium;cerium iv oxide cerium iii oxide cycle;composite material;conversion coating;corrosion;chemistry;chromatography;dielectric spectroscopy;dysprosium;electrical impedance;electrospinning;environmental chemistry;environmental impact statement;erosion corrosion;forensic engineering;heat treating;inorganic chemistry;intergranular corrosion;iron;lanthanide;laser;linear polarization;magnesium;manganese;materials science;metal;metallurgy;microstructure;mineralogy;morphology;nanocomposite;nanofiber;nanoparticle;nanotechnology;nickel;nitride;nitrogen;nuclear chemistry;nuclear magnetic resonance;nuclear physics;nuclear reactor;optical microscope;organic chemistry;particle size;performance;phosphorus;physical chemistry;physics;polarization;polymer;polymer chemistry;radiochemistry;residual stress;room temperature;scanning electron microscope;smart material;sol gel;surface modification;thin film;tissue engineering;titanium oxide;uranium;visual inspection;wear;x ray crystallography;x ray photoelectron spectroscopy\"},{\"text\":\"Science space (2104 2102 3324);Soil Science (Soil Science) ;Oceanography;Meteorology ;Hydrology ;Geophysics;Geology;Geography ;Geodesy;Geochemistry;Climatology ;Atmospheric sciences ;Space physiology ;Space medicine;Exobiology;Soil physics;Soil morphology and genesis;Soil mineralogy;Soil microbiology;Soil mechanics (agriculture);Soil engineering;Soil xonservation;Soil classification;Soil chemistry;soil cartography;Soil biology;Soil biochemistry;Marine aquaculture (oceanography);Renewable resources (oceanography);Marine Geology (oceanography);Underwater sounds;Shore and near-shore processes;Sea ice ;Sea-air interactions ;Physical oceanography ;Ocean-bottom processes ;Marine zoology ;Marine botany ;Descriptive oceanography;Chemical oceanography;Biological oceanography;Weather modification;Weather analysis;Tropical meteorology;Synoptic meteorology;Satellite meteorology ;Rocket meteorology;Radio meteorology;Radar meteorology;Polar meteorology;Operational forecasting (weather);Observation briefing (weather);Numerical weather prediction;Micrometeorology;Mesometeorología;Marine meteorology ;Industrial meteorology;Hydrometeorology ;Extended weather forecasting;Air pollution ;Agricultural mMeteorology;Transpiration;Surface waters;Soil moisture;Snow;Quality of water ;Precipitation;Permafrost;Limnology;Ice ;Hydrography;Hydrobiology;Groundwater;Glaciology ;Evaporation;Erosion (water);Tectonics;Solid-earth and geophysics;Seismology and seismic exploration;Paleomagnetism;Heat flow (earth);Gravity (earth) and gravity exploration ;Geomagnetism and magnetic explotarion;Well log analysis;Volcanology;Structural geology;Stratigraphy ;Sedimentology;Rock mechanisms;Remote sensing (geology);Photogeology;Petrology, sedimentary;Petrology, igneous and metamorphic;Petroleum geology ;Mineralogy;Mineral deposits;Glacial geology ;Geothermal processes and energy ;Geomorphology;Geological surveys;Geohydrology ;Environmental geology;Engineering geology;Coal geologi ;Areal geology;Topographic geography;Physical geography;Medical geography;Location theory;Land utilization ;Geography of natural resources;Geographical cartography;Biogeography ;Theoretical geodesy;Satellite geodesy ;Physical geodesy;Geodetic surveying;Geodesic photogrammetry;Geodesic navigation;Geodesic cartography;Geodesic astronomy ;Trace elements distribution;Stable isotopes;Organic geochemistry;Low temperatures geochemistry;High temperature geochemistry;Geochronology and radio isotope;Exploration geochemistry;Experimental petrology;Cosmochemistry ;Regional climatology;Physical climatology;Paleoclimatology;Microclimatology;Bioclimatology;Applied climatology;Analytical climatology;Solar wind;Radioactive transfer;Precipitation physics;Numerical modelling;Magnetospheric waves;Magnetospheric particles;Ionosphere;Geomagnetic pulsations;Dissemination (atmospheric);Cosmic rays ;Cloud physics;Aurora;Atmospheric turbulence\"}]";
//        String txt1 = "6111 aluminium alloy;active site;adhesion;adverse effect;analytical chemistry;aqueous solution;atomic force microscopy;calcium;ceramic materials;ceramic matrix composite;cerium;cerium iv oxide cerium iii oxide cycle;composite material;conversion coating;corrosion;chemistry;chromatography;dielectric spectroscopy;dysprosium;electrical impedance;electrospinning;environmental chemistry;environmental impact statement;erosion corrosion;forensic engineering;heat treating;inorganic chemistry;intergranular corrosion;iron;lanthanide;laser;linear polarization;magnesium;manganese;materials science;metal;metallurgy;microstructure;mineralogy;morphology;nanocomposite;nanofiber;nanoparticle;nanotechnology;nickel;nitride;nitrogen;nuclear chemistry;nuclear magnetic resonance;nuclear physics;nuclear reactor;optical microscope;organic chemistry;particle size;performance;phosphorus;physical chemistry;physics;polarization;polymer;polymer chemistry;radiochemistry;residual stress;room temperature;scanning electron microscope;smart material;sol gel;surface modification;thin film;tissue engineering;titanium oxide;uranium;visual inspection;wear;x ray crystallography;x ray photoelectron spectroscopy";
//        String txt2 = "Science space (2104 2102 3324);Soil Science (Soil Science) ;Oceanography;Meteorology ;Hydrology ;Geophysics;Geology;Geography ;Geodesy;Geochemistry;Climatology ;Atmospheric sciences ;Space physiology ;Space medicine;Exobiology;Soil physics;Soil morphology and genesis;Soil mineralogy;Soil microbiology;Soil mechanics (agriculture);Soil engineering;Soil xonservation;Soil classification;Soil chemistry;soil cartography;Soil biology;Soil biochemistry;Marine aquaculture (oceanography);Renewable resources (oceanography);Marine Geology (oceanography);Underwater sounds;Shore and near-shore processes;Sea ice ;Sea-air interactions ;Physical oceanography ;Ocean-bottom processes ;Marine zoology ;Marine botany ;Descriptive oceanography;Chemical oceanography;Biological oceanography;Weather modification;Weather analysis;Tropical meteorology;Synoptic meteorology;Satellite meteorology ;Rocket meteorology;Radio meteorology;Radar meteorology;Polar meteorology;Operational forecasting (weather);Observation briefing (weather);Numerical weather prediction;Micrometeorology;Mesometeorología;Marine meteorology ;Industrial meteorology;Hydrometeorology ;Extended weather forecasting;Air pollution ;Agricultural mMeteorology;Transpiration;Surface waters;Soil moisture;Snow;Quality of water ;Precipitation;Permafrost;Limnology;Ice ;Hydrography;Hydrobiology;Groundwater;Glaciology ;Evaporation;Erosion (water);Tectonics;Solid-earth and geophysics;Seismology and seismic exploration;Paleomagnetism;Heat flow (earth);Gravity (earth) and gravity exploration ;Geomagnetism and magnetic explotarion;Well log analysis;Volcanology;Structural geology;Stratigraphy ;Sedimentology;Rock mechanisms;Remote sensing (geology);Photogeology;Petrology, sedimentary;Petrology, igneous and metamorphic;Petroleum geology ;Mineralogy;Mineral deposits;Glacial geology ;Geothermal processes and energy ;Geomorphology;Geological surveys;Geohydrology ;Environmental geology;Engineering geology;Coal geologi ;Areal geology;Topographic geography;Physical geography;Medical geography;Location theory;Land utilization ;Geography of natural resources;Geographical cartography;Biogeography ;Theoretical geodesy;Satellite geodesy ;Physical geodesy;Geodetic surveying;Geodesic photogrammetry;Geodesic navigation;Geodesic cartography;Geodesic astronomy ;Trace elements distribution;Stable isotopes;Organic geochemistry;Low temperatures geochemistry;High temperature geochemistry;Geochronology and radio isotope;Exploration geochemistry;Experimental petrology;Cosmochemistry ;Regional climatology;Physical climatology;Paleoclimatology;Microclimatology;Bioclimatology;Applied climatology;Analytical climatology;Solar wind;Radioactive transfer;Precipitation physics;Numerical modelling;Magnetospheric waves;Magnetospheric particles;Ionosphere;Geomagnetic pulsations;Dissemination (atmospheric);Cosmic rays ;Cloud physics;Aurora;Atmospheric turbulence";
//        getEntities(txt);
//        p.CompareText(StringUtils.stripAccents(txt1), StringUtils.stripAccents(txt2));
//        corticalTest c = new corticalTest();
    //       List<Author> authors = c.getAuthors();
    //       Map<URI, String> unesco = c.getWords(150);
    //       Map<URI, Map<URI, Double>> result = c.getWeights(unesco, authors);
    //       for (Map.Entry<URI, Map<URI, Double>> r : result.entrySet()) {
    //          System.out.println(r.getKey());
    //         for (Map.Entry<URI, Double> score : r.getValue().entrySet()) {
    //             System.out.println("\t\t" + score.getKey() + "\t\t" + score.getValue());
    //        }
    //     }
    //   }
/*
    public Map<URI, Map<URI, Double>> getWeights(Map<URI, String> taxonomy, List<Author> authors) throws IOException, InterruptedException {
        Map<URI, Map<URI, Double>> authorScores = new HashMap<>();
        for (Author author : authors) {
            Map<URI, Double> scores = new HashMap<>();
            for (Map.Entry<URI, String> tax : taxonomy.entrySet()) {
                String keywords = StringUtils.stripAccents(author.getKeywords());
                String unescoWords = StringUtils.stripAccents(tax.getValue());
                int wordsAuthor = keywords.split(";").length;
                int wordsUnesco = unescoWords.split(";").length;
                System.out.println("[Unesco:" + wordsUnesco + ", Author:" + wordsAuthor + "]");
                double score = (double) processing.CompareText(keywords, unescoWords);
                scores.put(tax.getKey(), score);
            }
            authorScores.put(author.getURI(), scores);
        }
        return authorScores;
    }

    public Map<URI, String> getWords(int limit) {
        Map<URI, String> unescoTax = new HashMap<>();
        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
            UnescoNomeclature unesco = new UnescoNomeclature(conn);
            for (URI twoDigitResource : unesco.twoDigitResources()) {
                List<URI> fourDigitResources = unesco.narrow(twoDigitResource).stream()
                        .filter(uri -> !uri.stringValue().contains("99"))
                        .collect(Collectors.toList());
                List<URI> sixDigitResources = fourDigitResources.stream()
                        .map(fourDigit -> unesco.narrow(fourDigit))
                        .flatMap(List::stream)
                        .filter(uri -> !uri.stringValue().contains("99"))
                        .collect(Collectors.toList());

                List<URI> all = new ArrayList<>();
                all.addAll(fourDigitResources);
                all.addAll(sixDigitResources);
                List<String> words;

                if (limit == -1) {
                    words = all.stream().map(uri -> unesco.label(uri, "en").getLabel())
                            .collect(Collectors.toList());
                } else {
                    words = all.stream().map(uri -> unesco.label(uri, "en").getLabel())
                            .limit(limit).collect(Collectors.toList());
                }
                unescoTax.put(twoDigitResource, Joiner.on(";").join(words));
            }
        } catch (Exception ex) {
        }
        return unescoTax;
    }

    public List<Author> getAuthors() {
        try (RediRepository r = RediRepository.getInstance()) {
            Redi redi = new Redi(r);
            return redi.getAuthors();
        } catch (Exception ex) {
        }
        return Collections.emptyList();
    }
     */
}

    

