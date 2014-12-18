package net.josegarvin.recuperantDadesWeb;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class App {

  int numFoto = 0;
  String patro = null;
  static String dirSortida = "/home/b4tm4n/FOTOS/";

  public static void main(String[] args) {

    App program = new App();

    // Demanem la ruta al fitxer de contrasenyes.
    String rutaFitxer = program.demanarFitxer();
    
    //Demanem la ruta de sortida.
    //dirSortida = program.demanarDirSortida();

    File arxiu = null;
    FileReader fr = null;
    BufferedReader br = null;

    try {
      URL mainPage = new URL("http://projectes.iescendrassos.net/entrada/");

      arxiu = new File(rutaFitxer);
      fr = new FileReader(arxiu);
      br = new BufferedReader(fr);

      program.tractarFitxer(br, mainPage);

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
      System.out.println("Fitxer no trobat");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void tractarFitxer(BufferedReader br, URL mainURL) {

    String linia;
    try {
      patro = br.readLine();

      while ((linia = br.readLine()) != null) {

        String noEspais = eliminarEspais(linia);
        System.out.println();

        String parametres = generarURLParameters(getUsuari(noEspais, patro),
            getContrasenya(noEspais, patro));
        generarPeticio(mainURL, parametres);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public String getUsuari(String linia, String patro) {
    int tipusFitxer = determinarTipusFitxer(patro);
    if (tipusFitxer == 0) {
      int posSeparacio = linia.indexOf(":");
      if(posSeparacio != -1){
        return linia.substring(0, posSeparacio);
      }else{
        return null;
      }
      
    }
    if (tipusFitxer == 1) {
      int posSeparacio = linia.lastIndexOf(":");
      System.out.println("User---> "
          + linia.substring(posSeparacio + 1, linia.length()));
      return linia.substring(posSeparacio + 1, linia.length());

    } else {
      return null;
    }

  }

  public String getContrasenya(String linia, String patro) {
    int tipusFitxer = determinarTipusFitxer(patro);
    if (tipusFitxer == 0) {
      int posSeparacio = linia.indexOf(":");
      return linia.substring(posSeparacio + 1, linia.length());
    }
    if (tipusFitxer == 1) {
      int posSeparacio = linia.indexOf(":");
      System.out.println("PASS---> " + linia.substring(0, posSeparacio));
      return linia.substring(0, posSeparacio);
    } else {
      return "";
    }

  }

  public int determinarTipusFitxer(String patro) {

    if (patro.equalsIgnoreCase("usuari : contrasenya")) {
      return 0;
    }
    if (patro.equalsIgnoreCase("contrasenya : població : usuari")) {
      return 1;
    } else {
      System.out.println("El fitxer no compleix les condicions d'entrada!");
      return -1;
    }

  }

  public void generarPeticio(URL mainURL, String urlParameters) {

    String validationPath = "checklogin.php";

    URL url;
    try {

      CookieHandler
          .setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

      url = new URL(mainURL, validationPath);
      System.out.println(url.getPath());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type",
          "application/x-www-form-urlencoded");
      connection.setRequestProperty("charset", "utf-8");
      connection.setRequestProperty("Content-Length",
          "" + Integer.toString(urlParameters.getBytes().length));
      connection.setUseCaches(false);

      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();

      int resposta = connection.getResponseCode();
      System.out.println("Codi de resposta: " + resposta);

      BufferedReader in = new BufferedReader(new InputStreamReader(
          connection.getInputStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {

        if (inputLine.contains("img")) {
          String rutaRecurs = obtenirRutaRecurs(inputLine);
          URL rutaAbsolutaRecurs = new URL(url, rutaRecurs);
          descarregarFoto(rutaAbsolutaRecurs);
        }

      }

      in.close();

      connection.disconnect();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void descarregarFoto(URL rutaRecurs) {

    String dirSortida = "/home/b4tm4n/FOTOS/foto" + numFoto + ".png";

    BufferedImage image;
    try {
      image = ImageIO.read(rutaRecurs);
      ImageIO.write(image, "png", new File(dirSortida));
      numFoto++;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
      System.out.println("No s'ha pogut desar la fotografia.");
    }

  }

  public String obtenirRutaRecurs(String linia) {
    String rutaRecurs = "";

    int posInici = linia.indexOf("'") + 1;
    int posFinal = linia.lastIndexOf("'");

    return linia.substring(posInici, posFinal);
  }

  public String generarURLParameters(String user, String pass) {
    String urlParameters = "usuari=" + user + "&contrasenya=" + pass
        + "&Entrar=Entrar";
    return urlParameters;
  }

  public String eliminarEspais(String linia) {
    char espai = ' ';
    String liniaNeta = "";
    for (int i = 0; i < linia.length(); i++) {
      if (linia.charAt(i) != espai) {
        liniaNeta += linia.charAt(i);
      }
    }
    return liniaNeta;
  }

  public String demanarFitxer() {
    Scanner lector = new Scanner(System.in);
    System.out.println("Introdueïx la ruta al fitxer de contrasenyes: ");
    String ruta = lector.nextLine();
    File directori = new File(ruta);
    while(!directori.exists()){
      System.out.println("Fitxer inexistent! Torna-hi: ");
      ruta = lector.nextLine();
      directori = new File(ruta);
    }
    lector.close();
    return ruta;
  }
  
  public String demanarDirSortida() {
    Scanner lector = new Scanner(System.in);
    System.out.println("A on vols desar les fotos?");
    String ruta = lector.nextLine();
    File directori = new File(ruta);
    while(!directori.exists()){
      System.out.println("Aquest directori no existeix! Torna-hi: ");
      ruta = lector.nextLine();
      directori = new File(ruta);
    }
    lector.close();
    return ruta;
  }
}