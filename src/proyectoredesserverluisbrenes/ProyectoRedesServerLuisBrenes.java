/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoredesserverluisbrenes;

import Servidor.Servidor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdom.JDOMException;

/**
 *
 * @author Luis
 */
public class ProyectoRedesServerLuisBrenes {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor(5025);
            servidor.setVisible(true);
            servidor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            servidor.setLocationRelativeTo(null);
            servidor.setResizable(false);
        } catch (JDOMException ex) {
            Logger.getLogger(ProyectoRedesServerLuisBrenes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProyectoRedesServerLuisBrenes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
