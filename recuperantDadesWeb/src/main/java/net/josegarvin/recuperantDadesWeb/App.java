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

/**
 * Programa que a partir d'un fitxer amb usuaris i contrasenyes es descarrega la
 * imatge asociada a cadascuna de les credencials d'accès.
 * 
 * @author Jose Garvin Victoria.
 *
 */
public class App {

  /**
   * Variable per controlar el numero de fotos que s'han descarregat.
   */
  private static int numFoto = 0;

  /**
   * Variable on es desara el patro que s'ha de seguir per obtenir els usuaris i
   * les contrasenyes de fitxer.
   */
  private String patro = null;

  /**
   * String on es desa la ruta al directori de sortida on s'emmagatzemaran les
   * fotos descarregades.
   */
  private static String dirSortida = "fotos/";

  /**
   * Mètode principal del programa.
   * 
   * @param args
   *          --> .
   */
  public static void main(final String[] args) {

    App program = new App();

    // Demanem la ruta al fitxer de contrasenyes.
    String rutaFitxer = program.demanarFitxer();

    // Demanem la ruta de sortida.
    // dirSortida = program.demanarDirSortida();

    File arxiu = null;
    FileReader fr = null;
    BufferedReader br = null;

    try {
      URL mainPage = new URL("http://projectes.iescendrassos.net/entrada/");

      arxiu = new File(rutaFitxer);
      fr = new FileReader(arxiu);
      br = new BufferedReader(fr);

      program.tractarFitxer(br, mainPage);
      System.out.println("S'han descarregat " + numFoto + " fotos.");

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
      System.out.println("Fitxer no trobat");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Mètode que s'encarrega de tractar el fitxer de contrasenyes. Llegeix el
   * fitxer, genera el paràmetres de la petició, i envia la petició.
   * 
   * @param br
   *          --> Buffer de lectura del fitxer.
   * @param mainURL
   *          --> URL principal de la pàgina.
   */
  final void tractarFitxer(final BufferedReader br, final URL mainURL) {

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

  /**
   * Mètode que s'encarrega d'obtenir un usuari a partir d'una linia del fitxer
   * i un patró de format.
   * 
   * @param linia
   *          --> Linia del fitxer a tractar.
   * @param patroF
   *          --> Patró que segueix el fitxer.
   * @return --> Retorna un String corresponent a l'usuari trobat.
   */
  final String getUsuari(final String linia, final String patroF) {
    int tipusFitxer = determinarTipusFitxer(patroF);
    if (tipusFitxer == 0) {
      int posSeparacio = linia.indexOf(":");
      if (posSeparacio != -1) {
        return linia.substring(0, posSeparacio);
      } else {
        return null;
      }

    }
    if (tipusFitxer == 1) {
      int posSeparacio = linia.lastIndexOf(":");
      System.out.println("User---> "
          + linia.substring(posSeparacio + 1, linia.length()));
      return linia.substring(posSeparacio + 1, linia.length());

    } else {
      System.out.println("Patró incorrecte! No es pot obtenir l'usuari.");
      return null;
    }

  }

  /**
   * Mètode que s'encarrega d'obtenir una contrasenya a partir d'una linia del
   * fitxer i un patró de format.
   * 
   * @param linia
   *          --> Linia del fitxer a tractar.
   * @param patroF
   *          --> Patró que segueix el fitxer.
   * @return --> Retorna un String corresponent a la contrasenya trobada.
   */
  final String getContrasenya(final String linia, final String patroF) {
    int tipusFitxer = determinarTipusFitxer(patroF);
    if (tipusFitxer == 0) {
      int posSeparacio = linia.indexOf(":");
      return linia.substring(posSeparacio + 1, linia.length());
    }
    if (tipusFitxer == 1) {
      int posSeparacio = linia.indexOf(":");
      if (posSeparacio != -1) {
        System.out.println("PASS---> " + linia.substring(0, posSeparacio));
        return linia.substring(0, posSeparacio);
      }
      return null;
    } else {
      System.out.println("Patró incorrecte! No es pot obtenir la contrasenya.");
      return null;
    }

  }

  /**
   * Mètode per determinar el tipus de patró que segueix el fitxer donat.
   * 
   * @param patroF
   *          --> Primera linia (patro) del fitxer.
   * @return --> Retorna 0 si el fitxer segueix el patró "usuari:contrasenya" o
   *         1 si el fitxer segueix el patró "contrasenya:població:usuari".
   */
  final int determinarTipusFitxer(final String patroF) {

    if (patroF.equalsIgnoreCase("usuari:contrasenya")) {
      return 0;
    }
    if (patroF.equalsIgnoreCase("contrasenya:població:usuari")) {
      return 1;
    } else {

      return -1;
    }

  }

  /**
   * Mètode que s'encarrega de generar la petició a partir de l'URL principal de
   * la pàgina i els parametres a enviar.
   * 
   * @param mainURL
   *          --> URL principal de la pàgina.
   * @param urlParameters
   *          --> Parametres de la petició.
   */
  final void generarPeticio(final URL mainURL, final String urlParameters) {

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

  /**
   * Mètode que s'encarrega de descarreguar una fotografia a partir d'una ruta.
   * 
   * @param rutaRecurs
   *          --> Ruta al recurs.
   */
  final void descarregarFoto(final URL rutaRecurs) {

    String nomSortida = "foto" + numFoto + ".png";
    File directoriSortida = new File(dirSortida);
    BufferedImage image;
    try {
      image = ImageIO.read(rutaRecurs);
      if (!directoriSortida.exists()) {
        directoriSortida.mkdir();

      }

      ImageIO.write(image, "png", new File(directoriSortida + File.separator
          + nomSortida));
      numFoto++;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
      System.out.println("No s'ha pogut desar la fotografia.");
    }

  }

  /**
   * Mètode que encarregat de obtenir la ruta d'un recurs a partir d'una línia
   * del codi HTML de la pàgina.
   * 
   * @param linia
   *          --> Linia del cos de la pàgina de resposta.
   * @return --> Retorna un String corresponent a la ruta del recurs.
   */
  final String obtenirRutaRecurs(final String linia) {

    int posInici = linia.indexOf("'") + 1;
    int posFinal = linia.lastIndexOf("'");

    return linia.substring(posInici, posFinal);
  }

  /**
   * Mètode encarregat de generar la línia de parametres a enviar amb la
   * petició.
   * 
   * @param user
   *          --> String corresponent a l'usuari.
   * @param pass
   *          --> String corresponent a la contrasenya.
   * @return --> Retorna una linia amb les dades que cal enviar amb la petició.
   */
  final String generarURLParameters(final String user, final String pass) {
    String urlParameters = "usuari=" + user + "&contrasenya=" + pass
        + "&Entrar=Entrar";
    return urlParameters;
  }

  /**
   * Mètode per eliminar els espais de una linia.
   * 
   * @param linia
   *          --> Linia amb espais.
   * @return --> Retorna la linia sense espais.
   */
  final String eliminarEspais(final String linia) {
    char espai = ' ';
    String liniaNeta = "";
    for (int i = 0; i < linia.length(); i++) {
      if (linia.charAt(i) != espai) {
        liniaNeta += linia.charAt(i);
      }
    }
    return liniaNeta;
  }

  /**
   * Mètode per demanar la ruta al fitxer de contrasenyes a l'usuari.
   * 
   * @return --> Retorna un String corresponent a la ruta al fitxer de
   *         contrasenyes.
   */
  final String demanarFitxer() {
    Scanner lector = new Scanner(System.in);
    System.out.println("Introdueïx la ruta al fitxer de contrasenyes: ");
    String ruta = lector.nextLine();
    File directori = new File(ruta);
    while (!directori.exists()) {
      System.out.println("Fitxer inexistent! Torna-hi: ");
      ruta = lector.nextLine();
      directori = new File(ruta);
    }
    lector.close();
    return ruta;
  }

  /**
   * Mètode per demanar a l'usuari el directori de sortida de les fotos que es
   * descarreguin.
   * 
   * @return --> Retorna un String corresponent a la ruta de sortida de les
   *         fotos.
   */
  final String demanarDirSortida() {
    Scanner lector = new Scanner(System.in);
    System.out.println("A on vols desar les fotos?");
    String ruta = lector.nextLine();
    File directori = new File(ruta);
    while (!directori.exists()) {
      System.out.println("Aquest directori no existeix! Torna-hi: ");
      ruta = lector.nextLine();
      directori = new File(ruta);
    }
    lector.close();
    return ruta;
  }
}