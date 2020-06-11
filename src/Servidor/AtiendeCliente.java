package Servidor;

import Domain.ListaClienteJugador;

import Domain.Usuario;
import Utility.MyUtility;
import com.mysql.jdbc.PreparedStatement;
import com.sun.org.apache.xml.internal.serializer.ElemDesc;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class AtiendeCliente extends Thread {

    private Socket socket;
    private Element elemento;
    private Usuario usuarioAtentiendose;
    private boolean hiloSalir;

    private String opcionQuiero;
    // private ListaClienteJugador listaPartidasJugadoresSingleton;

    private DataOutputStream sendArchivo;
    private DataInputStream receiveArchivo;

    public AtiendeCliente(Socket socket) throws IOException, JDOMException {

        super("Hilo Servidor");
        this.hiloSalir = true;
        this.socket = socket;
        this.usuarioAtentiendose = new Usuario();
        // this.listaPartidasJugadoresSingleton = ListaClienteJugador.obtenerInstancia();

        this.sendArchivo = new DataOutputStream(this.socket.getOutputStream());
        this.receiveArchivo = new DataInputStream(this.socket.getInputStream());

    }

    public void run() {
        try {

            System.out.println("Servidor ejecutando");

            while (this.hiloSalir) {
                System.out.println("Cliente acceptado");

                this.opcionQuiero = "";

                this.opcionQuiero = this.receiveArchivo.readUTF();

                System.out.println(this.opcionQuiero + "  opcion enviada");

                switch (this.opcionQuiero) {

                    case "quiensoy":
                        
                        
                        this.usuarioAtentiendose.setNombre(this.receiveArchivo.readUTF());
                        System.out.println(this.usuarioAtentiendose.getNombre());
                        break;
                    case "logear":

                        System.out.println("Entre a loguarme");

                        // this.usuarioAtentiendose = xmlAUsuario(element);
                        this.usuarioAtentiendose.setNombre(this.receiveArchivo.readUTF());

                        this.usuarioAtentiendose.setContraseña(this.receiveArchivo.readUTF());

                        System.out.println("nombre:" + this.usuarioAtentiendose.getNombre());
                        System.out.println("contaseña:" + this.usuarioAtentiendose.getContraseña());
                        boolean encotrado = false;

                        Conectar conect = new Conectar();
                        Connection conectar;
                        try {

                            conectar = conect.conexion();
                            Statement pst = conectar.createStatement();
                            ResultSet rs = pst.executeQuery("call get_Usuario('" + this.usuarioAtentiendose.getNombre() + "','" + this.usuarioAtentiendose.getContraseña() + "')");

                            String nombreRe = "";
                            String contrasenaRe = "";
                            while (rs.next()) {
                                nombreRe = rs.getString("nombre");
                                contrasenaRe = rs.getString("contrasena");
                                //System.out.println("nombre = " + nombreRe + " contrasena" + apellidoRe);
                            }

                            Element elementoEnviar = new Element("Logueo");
                            Element accion = new Element("accion");

                            if (nombreRe.equals("")) {
                                this.sendArchivo.writeUTF("no logueo");

                            } else {
                                this.sendArchivo.writeUTF("si logueo");
                                //listarArchivos();
                            }

                            listarArchivos();

                        } catch (SQLException ex) {
                            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        break;

                    case "cargarArchivo":

                        recibirArchivo();

                        break;

                    case "pedirArchivo":
                        System.out.println("Entre a pedir archivo");
                        enviarArchivo();
                        break;

                }

            }

        } catch (IOException ex) {
            Logger.getLogger(AtiendeCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // run

    private Element stringToXML(String stringMensaje) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        StringReader stringReader = new StringReader(stringMensaje);
        Document doc = saxBuilder.build(stringReader);
        return doc.getRootElement();
    } // stringToXML 

    private Usuario xmlAUsuario(Element elementoActual) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setNombre(elementoActual.getAttributeValue("nombre"));
        usuarioActual.setContraseña(elementoActual.getChild("Contrasena").getValue());

        return usuarioActual;

    } // xmlAEstudiante

    private Element generarUsuarioXML(Usuario usuario) {

        Element mUsuario = new Element("Usuario");
        mUsuario.setAttribute("nombre", usuario.getNombre());

        Element mContrasena = new Element("Contrasena");
        mContrasena.addContent(usuario.getContraseña());

        mUsuario.addContent(mContrasena);

        return mUsuario;

    } // generEstudianteXML

    private String xmlToString(Element element) {
        XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
        String xmlStringElement = output.outputString(element);
        xmlStringElement = xmlStringElement.replace("\n", "");
        return xmlStringElement;
    } // xmlToString

    public Usuario getUsuarioAtentiendose() {
        return usuarioAtentiendose;
    }

    public void setUsuarioAtentiendose(Usuario usuarioAtentiendose) {
        this.usuarioAtentiendose = usuarioAtentiendose;
    }

    public String getOpcionQuiero() {
        return opcionQuiero;
    }

    public void setOpcionQuiero(String opcionQuiero) {
        this.opcionQuiero = opcionQuiero;
    }

    public void recibirArchivo() throws IOException {/* en el servidor*/
        String filename = this.receiveArchivo.readUTF();

        byte readbytes[] = new byte[1024];
        InputStream in = this.socket.getInputStream();

        try (OutputStream file = Files.newOutputStream(Paths.get("usuarios" + "\\" + this.usuarioAtentiendose.getNombre() + "\\" + filename))) {

            for (int read = -1; (read = in.read(readbytes)) >= 0;) {
                file.write(readbytes, 0, read);
                if (read < 1024) {
                    break;
                }
            }
            file.flush();
            file.close();
        }

        //this.receiveArchivo = new DataInputStream(this.socket.getInputStream());
        in.close();
        //this.accion = "";
        filename = "";
        System.out.println("Acaba de recibir");
        this.hiloSalir = false;

    }

    public void listarArchivos() throws IOException {

        String nombreFin = "usuarios\\" + this.usuarioAtentiendose.getNombre();
        File carpeta = new File(nombreFin);
        String[] listado = carpeta.list();
        if (listado == null || listado.length == 0) {
            this.sendArchivo.writeInt(listado.length);
            System.out.println("No hay elementos dentro de la carpeta actual");
        } else {
            this.sendArchivo.writeInt(listado.length);
            for (int i = 0; i < listado.length; i++) {

                this.sendArchivo.writeUTF(listado[i]);

                // this.send.println(xmlToString(elementoActual));
            }
        }
    }

    public void enviarArchivo() throws FileNotFoundException, IOException {

        String filename = this.receiveArchivo.readUTF();
        System.out.println("usuarios" + "\\" + this.usuarioAtentiendose.getNombre() + "\\" + filename);
        File archivo = new File("usuarios" + "\\" + this.usuarioAtentiendose.getNombre() + "\\" + filename.trim());

        if (archivo.exists()) {
            System.out.println("si entre a ver el archivo");
            //this.sendArchivo.writeUTF("hola");
            byte byteArray[] = null;
            byteArray = Files.readAllBytes(Paths.get("usuarios" + "\\" + this.usuarioAtentiendose.getNombre() + "\\" + filename.trim()));
            this.sendArchivo.write(byteArray);
            this.sendArchivo.flush();
            System.out.println("llegue salir");
            filename = "";
        } else {
            System.out.println("no entre al if del existe el archivo");
            //this.sendArchivo.writeUTF();
        }
        
        this.hiloSalir = false;
    }

} // fin clase
