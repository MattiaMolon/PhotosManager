package fotomanager;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import org.apache.commons.io.FilenameUtils;



public class FotoManager {

    public static void main(String[] args) throws IOException, ImageProcessingException{
       
        //recupero la path della cartella su cui devo lavorare
        String FolderPath = getFolderPath();

        //se è stato selezionato un path posso cominciare a lavorare
        if(!"notSelected".equals(FolderPath)){
            
            //recupero la cartella a cui l'utente si riferisce
            File folder = new File(FolderPath);
            
            //cerco all'interno della cartella gli elementi che mi interessano
            Vector<File> folderFiles = getFolderFiles(folder);
            
            //chiamo la funzione che rinomina le foto
            renameByTags(folderFiles);
            
        }

    }
    
    
    //funzione che fa selezionare all'utente la cartella su cui operare
    //restituisce la path della cartella oppure "notSelected" nel caso non
    //venga selezionata per un qualche motivo
    public static String getFolderPath(){
        
        //int sul controllo della scelta
        int choose, exit;
        String path = "notSelected";
        do{
            //resetto le variabili ogni volta per evitare loop
            choose = -1; exit = -1;
            
            //Create a file chooser
            JFileChooser fc = new JFileChooser();
        
            //setto alcuni accorgimenti al File Chooser
            fc.setMultiSelectionEnabled(false);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setApproveButtonText("select");
            fc.setCurrentDirectory(new File("C://"));
        
            //visualizzo il file chooser
            fc.showOpenDialog(null);
        
            //seleziono la cartella scelta nel FileChooser e chiedo all'utente
            //se è sicuro della scelta
            File folder = fc.getSelectedFile();
            if (folder == null){
                exit = JOptionPane.showConfirmDialog(null, "Are you sure do you want to exit?",
                        "EXIT", YES_NO_OPTION);
            }else{
                choose = JOptionPane.showConfirmDialog(null, "Are you sure to operate on:\n" 
                        + folder.getPath(), "ALERT!", YES_NO_OPTION);
            }
            
            //setto il path a quello scelto
            if (choose == JOptionPane.YES_OPTION) path = folder.getPath();
            
        //esco dal ciclo solamente se l'utente è sicuro della sua scelta o
        //se vuole uscire dal programma
        }while(choose == JOptionPane.NO_OPTION || exit == JOptionPane.NO_OPTION);
        
        return path;
    }

    
    //funzione che prende in ingresso la path di una cartella e restituisce 
    //un vettore con tutti i file (FOTO E BASTA PER ORA) all'interno della cartella
    private static Vector<File> getFolderFiles(File folder) {
        
        //recupero dalla cartella la lista di tutti i file.
        String[] tmp = folder.list();
           
        Vector <File> files = new Vector<>();
        
        //ciclo sui file cercando i file che mi interessano (PER ORA SOLO FOTO) 
        //e li inserisco nel vettore dei files
        for(String file : tmp){
            if (file.endsWith(".JPG") || file.endsWith(".jpg") ||
                file.endsWith(".JPEG") || file.endsWith(".jpeg") ||
                file.endsWith(".PNG") || file.endsWith(".png") ||
                file.endsWith(".MOV") || file.endsWith(".mp4") ||
                file.endsWith(".gif")){
                files.add(new File(folder.getPath() + "/" + file));
            }
        }
        
        return files; 
    }

    
    //rinomino la foto secondo il tag della data ad esso associato. 
    //nel caso non ritrovi la foto provo a ricavarlo dal nome
    private static void renameByTags(Vector<File> folderFiles) 
                        throws IOException, ImageProcessingException {
        
        //scorro gli elementi di Folderfiles per leggere i metadati
        for(File file : folderFiles){
            
            //ottengo l'oggetto metadata che contiene tutti i metadati del file
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            
            //creo una variabile booleana che mi definisce se sono riuscito a 
            //trovare la data tra i tag dei metadati o se è necessario cercare nel nome
            boolean finito = false;
            
            for (Directory directory : metadata.getDirectories()) {
               for (Tag tag : directory.getTags()) {
                 
                   //se trovo il tag inerente alla data della foto
                   if("Date/Time".equals(tag.getTagName())){
                        
                        //ho trovato il tag con la data e quindi posso cambiare il nome
                        finito=true;
                        
                        //trovo sia il filePath che il fileExt che 
                        //mi serviranno per rinominare il file
                        String filePath = FilenameUtils.getFullPath(file.getPath());
                        String fileExt = FilenameUtils.getExtension(file.getPath());
                       
                        //leggo il tag
                        String title = tag.getDescription();
                        title = title.replace(":", "-");
                        
                        //recupero l'anno dal titolo del tag
                        String photoYear = title.substring(0, 4);
                        
                        //creo nuova cartella dove mettere la foto e la inserisco
                        File newFolder = new File (filePath + photoYear);
                        if(!newFolder.exists())
                            newFolder.mkdir();
                        
                        //sposto la foto con le informazioni ottenute
                        file.renameTo(new File(newFolder+ "/" + title + "." + fileExt));
                   }
                }
            }
            
            //se non ho trovato il nome tra i vari tag lo cerco nel nome
            if (!finito)
                renameByName(finito, file);
            
            //se non trovo il nome nemmeno nel nome metto le foto in una
            //cartella "data undefined"
            if (!finito){
                
                //prendo la path di dove sono
                String filePath = FilenameUtils.getFullPath(file.getPath());
                
                //creo il folder "data undefined"
                File undefFolder = new File(filePath + "data undefined");
                if(!undefFolder.exists())
                            undefFolder.mkdir();
                
                //sposto la foto con le informazioni ottenute
                file.renameTo(new File(undefFolder+ "/" + file.getName()));
            }
                
            
        }
    }

    
    //rinomino la foto secondo il nome della foto stessa 
    //nel caso non la trovassi non faccio nulla
    private static void renameByName(boolean finito, File file) {
        String nome = file.getName();
        String anno="3000", mese="--", giorno, ora, minuto, secondo, title="notFound";        
        
        //ricavo anno, mese, giorno etc. dal nome
        if (nome.startsWith("200") || nome.startsWith("201")){
            anno = nome.substring(0, 4);
            mese = nome.substring(4, 6);
            giorno = nome.substring(6, 8);
            ora = nome.substring(9, 11);
            minuto = nome.substring(11, 13);
            secondo = nome.substring(13, 15);
            
            title = anno +"-"+ mese +"-"+ giorno +" "+ ora +"-"+ minuto +"-"+ secondo;
        }
        else if (nome.startsWith("IMG_") || 
                (nome.startsWith("IMG-") && !nome.contains("WA"))){
            anno = nome.substring(4, 8);
            mese = nome.substring(8, 10);
            giorno = nome.substring(10, 12);
            ora = nome.substring(13, 15);
            minuto = nome.substring(15, 17);
            secondo = nome.substring(17, 19);
            
            title = anno +"-"+ mese +"-"+ giorno +" "+ ora +"-"+ minuto +"-"+ secondo;
        }
        else if (nome.startsWith("IMG-") && nome.contains("WA")){
            anno = nome.substring(4, 8);
            mese = nome.substring(8, 10);
            giorno = nome.substring(10, 12);
            ora = nome.substring(15, 19);
            
            title = anno +"-"+ mese +"-"+ giorno +" "+ ora;
        }
        
        //controllo se è il caso di cambiare il nome della foto oppure no
        if (!title.equals("notFound") && 
                (Integer.parseInt(anno) >= 2000 && Integer.parseInt(anno) <= 2030) &&
                (!mese.contains("-"))){
            
            //l'ho trovato e rinomino la foto
            finito = true;
            
            //trovo sia il filePath che il fileExt che 
            //mi serviranno per rinominare il file
            String filePath = FilenameUtils.getFullPath(file.getPath());
            String fileExt = FilenameUtils.getExtension(file.getPath());
                        
            //recupero l'anno dal titolo del tag
            String photoYear = anno;
                        
            //creo nuova cartella dove mettere la foto e la inserisco
            File newFolder = new File (filePath + photoYear);
            if(!newFolder.exists())
                newFolder.mkdir();
                        
            //sposto la foto con le informazioni ottenute
            file.renameTo(new File(newFolder+ "/" + title + "." + fileExt));
        }
    }
        
}
    


