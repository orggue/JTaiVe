import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.concurrent.Executors;

/**
 * Created by jorge on 27/11/14.
 */
public class Rows extends SwingWorker<Void, Integer> implements ActionListener{
    private JPanel panel1;
    private JProgressBar pbDownload;
    private JButton btStop;
    private JButton btReload;
    private JLabel txtName;
    private JLabel txtDownload;
    private JButton btDelete;
    private boolean stopped;
    private Window window;
    private String path,primaryPath;
    private URL url;
    private Logger log4j = Logger.getLogger(Rows.class.getName());

    public URL getUrl() {
        return url;
    }

    public String getPrimaryPath() {
        return primaryPath;
    }

    public Rows(Window window){
        try {
            log4j.addAppender(new FileAppender(new PatternLayout(), "JTaiVe.log", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.window = window;
        init();
        /*ExecutorService threadPool = Executors.newFixedThreadPool(numberThreads);
        threadPool.submit(this);*/
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public void init(){
        PropertyConfigurator.configure(getClass().getResource("log4j.properties"));

        btStop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btReload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //action listener
        btReload.addActionListener(this);
        btStop.addActionListener(this);
        btDelete.addActionListener(this);

        path = "";
    }

    public void activateDeactivate(boolean activate){
        stopped = activate;
        btReload.setEnabled(activate);
        btStop.setEnabled(!activate);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == btStop){
            stop();
            return;
        }
        if (actionEvent.getSource() == btReload){
            reload();
            return;
        }
        if (actionEvent.getSource() == btDelete){
            delete();
            return;
        }
    }

    public void stop(){
        if(JOptionPane.showConfirmDialog(null,"¿Quieres cancelar la descarga?",
                "¿Estas seguro?",JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION){
            return;
        }
        activateDeactivate(true);
        log4j.info("Descarga pausada");
    }

    @Override
    protected Void doInBackground() throws Exception {
        url = new URL(window.getUrl().toString());
        URLConnection urlConnection = url.openConnection();

        int fileWeight = urlConnection.getContentLength();
        String fileName = URLDecoder.decode(url.getFile(),"UTF-8");
        fileName = fileName.substring(fileName.lastIndexOf("/")+1);
        txtName.setText(fileName);
        txtDownload.setText(fileWeight + " bytes");

        primaryPath = window.getPath();
        path = window.getPath() + File.separator + fileName;

        InputStream inputStream = url.openStream();
        FileOutputStream fileOutputStream = new FileOutputStream(path);

        byte [] bytes = new byte[2048];
        int weight = 0, progress = 0;
        log4j.info("Descarga iniciada "+ fileName);
        while ((weight = inputStream.read(bytes)) != -1 && !stopped){
            Thread.sleep(500);
            fileOutputStream.write(bytes,0,weight);

            progress += weight;
            setProgress(progress*100/fileWeight);

            txtDownload.setText(FileUtils.byteCountToDisplaySize(progress) + " de " + FileUtils.byteCountToDisplaySize(fileWeight));
        }

        inputStream.close();
        fileOutputStream.close();

        if (stopped){
            setProgress(0);

            txtName.setForeground(Color.decode("#F44336"));
            txtDownload.setForeground(Color.decode("#F44336"));

            log4j.info("Descarga Cancelada "+ fileName);
        }else {
            setProgress(100);

            txtName.setForeground(Color.decode("#1565C0"));
            txtDownload.setForeground(Color.decode("#1565C0"));
            activateDeactivate(true);

            log4j.info("Descarga finalizada "+ fileName);
        }
        return null;
    }

    public void delete(){
        int option = JOptionPane.showConfirmDialog(null, "¿Deseas eliminar el archivo origen y borrarlo de la lista?",
                "Confirmar", JOptionPane.YES_NO_CANCEL_OPTION);

        if (option == JOptionPane.CANCEL_OPTION)
            return;

        activateDeactivate(true);
        log4j.info("Descarga elinimada");

        if (option == JOptionPane.YES_OPTION){
            new File(path).delete();
            log4j.info("Fichero elinimado " + path);
        }

        window.deleteRow(this);
    }

    //todo reload download
    public void reload (){
        activateDeactivate(false);

        txtName.setForeground(Color.decode("#000"));
        txtDownload.setForeground(Color.decode("#000"));

        window.reloadRow(this);
    }

    public JProgressBar getPbDownload() {
        return pbDownload;
    }
}
