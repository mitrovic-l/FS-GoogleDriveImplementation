package implementation;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import specifikacija.FileStorageSpecification;
import specifikacija.SpecificationManager;
import specifikacija.izuzeci.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DriveImplementation implements FileStorageSpecification {

    private String storageRoot;
    private List<String>restrikcije = new ArrayList<>();
    private long maxStorageSize;
    private long currentSize;
    private File storageFile;
    private File metadata;
    private HashMap<String, Integer> directoryMap = new HashMap<>();
    private String separator = java.io.File.separator;
    private Drive service;
    private String currID;
    private String storagesID;
    private String currentStorageID = null;
    java.io.File datameta = new java.io.File("datameta");

    static {

        SpecificationManager.registerImplementation(new DriveImplementation());
    }

    private DriveImplementation(){
    }

    public void settings(){
        this.service = null;
        try {
            this.service = getDriveService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "My project";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = DriveImplementation.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public void setFileName(String fileName) {

    }

    @Override
    public void createFile(String fileName) throws InvalidExtension, DuplicateName {
        String[] ext = fileName.split("\\.");
        int extLength = ext.length;
        if (!(extLength<=0)){
            if (this.restrikcije.contains(ext[1])){
                System.out.println("Nedozvoljena ekstenzija, ne kreira se fajl!");
                return;
            }
        }
        File newFile = new File();
        File file = null;
        newFile.setName(fileName);
        newFile.setMimeType("application/octet-streamr");
        newFile.setParents(Collections.singletonList(getCurrID()));
        try {

            if (currID.equals(currentStorageID)) {
                file = service.files().create(newFile)
                        .setFields("id, parents")
                        .execute();
                System.out.println("Kreiran fajl: " + newFile.getName());
            }
            else {
            Integer brojFajlova = this.directoryMap.get(currID);
            if (brojFajlova != null && brojFajlova > 0) {
                file = service.files().create(newFile)
                        .setFields("id, parents")
                        .execute();
                System.out.println("Kreiran fajl: " + newFile.getName());
                this.directoryMap.put(currID, --brojFajlova);
            } else {
                System.out.println("Prekoracena kolicina fajlova za direktorijum.");
                return;
            }
        }

            //   directoryMap.get(currID) = directoryMap.get(currID)-Integer.parseInt(String.valueOf(newFile.getSize()));
        } catch (IOException e) {
            // TODO(developer) - handle error appropriately
            try {
                throw e;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public String getFileID(String fileName){
        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        for (File file : files) {
            if(file.getName().equals(fileName)){
                return file.getId();
            }
        }
        System.out.println("Ne postoji fajl sa prosledjenim imenom!");
        return null;
    }

    @Override
    public void deleteFile(String fileName) throws NoSuchFile {
        String fileID = getFileID(fileName);
        try {
            service.files().delete(fileID).execute();
            System.out.println("Uspesno izbrisan fajl.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void renameFile(String fileName, String newFileName) throws DuplicateName, NoSuchFile {
        try {
            throw new UnsupportedOperation();
        } catch (UnsupportedOperation e) {
            throw new RuntimeException(e);
        }
        //izgleda da nije moguce promeniti ime fajlu <----- proveriti dodatno
    }


    @Override
    public void moveFile(String fileName, String newDirPath) throws DuplicateName, NoSuchFile, OversizeException {
        String fileID = getFileID(fileName);
        String newFolderID = getFileID(newDirPath);
        File file = null;
        try {
            file = service.files().get(fileID).setFields("parents").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder previousParents = new StringBuilder();
        for(String parent : file.getParents()){
            previousParents.append(parent);
            previousParents.append(',');
        }
        try{
            file = service.files().update(fileID, null)
                    .setAddParents(newFolderID)
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, parents")
                    .execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Uspesno premesten fajl.");
    }

    @Override
    public String getFilePath(String fileName) {
        return null;
    }

    public File getFile(String filename){
        FileList result = null;
        File targetFile = null;
        try {
            result = service.files().list()
                    .setFields("files(id, name, mimeType, size, description)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> files = result.getFiles();
        for(File f: files){
            if(f.getName().equalsIgnoreCase(filename)) {
                targetFile = f;
//                System.out.println("File/Folder " + f.getName() + " found and returned!");
            }
        }
        return targetFile;
    }

    @Override
    public boolean downloadFile(String filename) {
        String fileId = getFileID(filename);
        fileId=getFileID(filename);
        String fileMime = getFile(filename).getMimeType();
        OutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(System.getProperty("user.home") + java.io.File.separator + filename);
            //OutputStream outputStream = new ByteArrayOutputStream();
            //service.files().get(fileId)
            //      .executeMediaAndDownloadTo(outputStream);

            //   FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + java.io.File.separator + filename);
            // fileWriter.write(String.valueOf(outputStream));
            // fileWriter.close();
            // outputStream.close();
            //  System.out.println("File skinut na : "+ System.getProperty("user.home") + java.io.File.separator);
            try {
                if (fileMime.contains("vnd.google-apps.document")) {
//                    System.out.println("File is a google doc");
                    service.files().export(fileId, "application/pdf")
                            .executeMediaAndDownloadTo(outputStream);
                    outputStream.flush();
                    outputStream.close();
//                    System.out.println("File downloaded!");
                } else {
                    service.files().get(fileId)
                            .executeMediaAndDownloadTo(outputStream);
                    outputStream.flush();
                    outputStream.close();
//                    System.out.println("File downloaded!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean uploadFile(String fileName) throws UnsupportedOperation {
        File fileMetadata = new File();
        fileMetadata.setName("dejane");
        fileMetadata.setParents(Collections.singletonList(getCurrID()));
        // File's content.
        java.io.File filePath = new java.io.File("C:\\Users\\User\\deki.txt");
        // Specify media type and file-path for file.
        FileContent mediaContent = new FileContent("text/plain", filePath);
        try {
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id , parents")
                    .execute();
            System.out.println("File ID: " + file.getId());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void storageInit(String path, int storageSize, String restriction) {
        //pozvati fju za createDir

        //pitati da li Storages vec postoji, ukoliko ne znaci da se prvi put pravi neki storage
        //ne smemo imati pristup u Storages vec samo do storage-a

        settings();
        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                if (file.getName().equals("Storages")){
                    System.out.println("Pronadjen storages---------------");
                    storagesID = file.getId();
                    // createDirectory("asd");
                    this.createStorage(path, storageSize, restriction);
                    break;
                }
            }
            if (storagesID==null){
                System.out.println("StorageID je null");
                // File's metadata.
                File fileMetadata = new File();
                fileMetadata.setName("Storages");
                fileMetadata.setMimeType("application/vnd.google-apps.folder");
                try {
                    File file = service.files().create(fileMetadata)
                            .setFields("id")
                            .execute();
                    storagesID = file.getId();

                    //  System.out.println("Folder ID: " + file.getId());
                    //return file.getId();
                } catch (IOException e) {
                    // TODO(developer) - handle error appropriately
                    try {
                        throw e;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }


                this.createStorage(path, storageSize, restriction);
            }
        }

    }

    private java.io.File writeToMetaData(Integer size, String... restrictions){
        java.io.File file = getDatameta();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            String[] r = restrictions[0].split(",");
            this.restrikcije.addAll(List.of(r));
            System.out.println("Lista nedozvoljenih ekstenzija: ");
            for (String s:this.restrikcije){
                System.out.println(s);
            }
            bw.write("Restrikcije: " +Arrays.toString(restrictions));
            bw.newLine();
            bw.write("Velicina skladista: "+size.toString());
            bw.newLine();
            bw.write("Direktorijumi: ");
            bw.newLine();
            bw.close();
            setDatameta(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        setDatameta(file);
        return file;
    }

    private void createStorage(String name, int size, String... restrictions){

        System.out.println("Uso u create storage ----------- 1");
        FileList result = null;
        try {
            result = this.service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        boolean ok = true;
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (file.getName().equals(name)) {
                    setCurrID(file.getId());
                    System.out.println("Pronadjen storage s istim imenom");
                    return;
                }
            }
        }
        System.out.println("Making storage directory");
        File dir = new File();
        File file = null; //ovo nam je storage
        dir.setName(name);
        dir.setMimeType("application/vnd.google-apps.folder");
        dir.setParents(Collections.singletonList(storagesID));
        try {

            file = service.files().create(dir)
                    .setFields("id, parents")
                    .execute();
            //System.out.println(file.getName());
            File metaFileMetadata = new File();
            File metaFile = null;
            FileContent content = new FileContent("text/plain", writeToMetaData(size, restrictions));
            metaFileMetadata.setName("data.meta");
            metaFileMetadata.setMimeType("application/octet-stream");
            System.out.println("Pravljenje metadata u "+file.getId() + file.getName());
            metaFileMetadata.setParents(Collections.singletonList(file.getId()));
            setCurrID(file.getId());
            setCurrentStorageID(file.getId());
            try {
                metaFile = service.files().create(metaFileMetadata, content)
                        .setFields("id, parents")
                        .execute();

            }catch (Exception exception){}
        } catch (IOException e) {
            // TODO(developer) - handle error appropriately
            try {
                throw e;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    //funkcija koja ce pri kreiranju fajla proveriti da li u trenutnom diru ima dovoljno prostora za njega
    private boolean remainingSpace(File newFile){
        String currDirID = getCurrID();
        return false;
    }

    private void updateMetaData(String name, Integer maxSize){
        java.io.File zaRead = getDatameta();
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(zaRead,true));
            bufferedWriter.write(name+" : "+maxSize.toString());
            bufferedWriter.newLine();
            bufferedWriter.close();
            setDatameta(zaRead);
            // First retrieve the file from the API.
            File file = service.files().get(getFileID("data.meta")).execute();
            // File's new content.
            File newMeta = new File();
            FileContent mediaContent = new FileContent("text/plain", zaRead);
            newMeta.setName(file.getName());
            newMeta.setParents(file.getParents());
            newMeta.setMimeType("application/octet-stream");
            //  ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", nFile);
            // Send the request to the API.
            File updatedFile = service.files().update(getFileID("data.meta"), newMeta, mediaContent).execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Uspesno update-ovan data.meta fajl.");
    }

    @Override
    public void createDirectory(String name, Integer... numOfFiles) throws DuplicateName {
        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        boolean ok = true;
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (file.getName().equals(name)) {
                    return;
                    //ne pravi nista
                }
            }
        }
        File dir = new File();
        File file = null;
        dir.setName(name);
        dir.setMimeType("application/vnd.google-apps.folder");
        dir.setParents(Collections.singletonList(getCurrID()));
        try {

            file = service.files().create(dir)
                    .setFields("id, parents")
                    .execute();
            System.out.println("Kreiran dir");
            if(numOfFiles.length<=0) {
                updateMetaData(name, 4096);
                this.directoryMap.put(file.getId(), 4096);
            }
            else {
                updateMetaData(name, numOfFiles[0]);
                this.directoryMap.put(file.getId(), numOfFiles[0]);
            }
        } catch (IOException e) {
            // TODO(developer) - handle error appropriately
            try {
                throw e;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    @Override
    public void deleteDirectory(String dirName) throws NoSuchFile, InvalidDelete {
        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        for (File f : files){
            if (f!=null && f.getParents()!=null && f.getParents().contains(getFileID(dirName))){
                f.getParents().remove(getFileID(dirName));
                deleteFile(f.getName());
            }
        }
        deleteFile(dirName);
    }

    @Override
    public void setUnsupportedExtensions(String[] unsupportedExtensions) {

    }

    @Override
    public String getDirectoryPath(String name) {
        return null;
    }

    @Override
    public void downoloadDirectory(String sourcePath, String destinationPath) throws UnsupportedOperation {
        throw new UnsupportedOperation();
    }

    @Override
    public void renameDirectory(String dirName, String novoIme) throws UnsupportedOperation, DuplicateName {
        throw new UnsupportedOperation();
    }

    @Override
    public List<String> listAll(String... path) {

        FileList result = null;
        String dir=null;
        List<File>lista = new ArrayList<>();
        if (path.length>0)
            dir= getFileID(path[0]);
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        boolean ok = true;
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (path.length<=0) {
                    if (file != null && file.getParents() != null && file.getParents().contains(currID)) {
                        lista.add(file);
                        //treba zameniti sa Listom i da se zove pomocna funkcija za ispis------
                    }
                } else {
                    if (file != null && file.getParents() != null && file.getParents().contains(dir)) {
                        lista.add(file);
                    }
                }
            }
        }
        pomZaIspis(lista);
        List<String>fileNames = new ArrayList<>();
        for (File file : lista){
            fileNames.add(file.getName());
        }
        return fileNames;
    }
    boolean name = true;
    boolean size,parent,date;
    public void pomZaIspis(List<File>files){
        //List<File> listWithoutDuplicates2 = ispis.stream()
        //                .distinct()
        //                .collect(Collectors.toList());
        List<File>pomFiles = files.stream()
                .distinct().collect(Collectors.toList());

        if (pomFiles==null || pomFiles.isEmpty()){
            return;
        }
        for (File file : pomFiles){
            if (name){
                System.out.print(file.getName()+ "  :  ");
            }
            if (size){
                System.out.print(file.getSize()+ "<-- size   ");
            }
            if (parent){
                System.out.print(file.getParents().toString()+ "<-- parent   ");
            }
            if (date){
                System.out.print(file.getModifiedTime().toStringRfc3339()+ "<-- date   ");
            }
            System.out.println();
        }
    }

    public List<File> pomListFull(String currDirID, List<File>lista){
        System.out.println("Uso u pomListFull za :"+ currDirID);
        FileList result = null;
        try{
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File>files = result.getFiles();
        if (files == null||files.isEmpty()) return null;
        for (File file : files){
            if (file!=null && file.getParents()!=null && file.getParents().contains(currDirID)&& file.getMimeType()!=null  &&file.getMimeType().contains("folder")){
                lista.add(file);
                lista.addAll(pomListFull(getFileID(file.getName()), lista));

            }
            if (file!=null&&file.getParents()!=null&&file.getParents().contains(currDirID)&& file.getMimeType()!=null  && !file.getMimeType().contains("folder")) {
                lista.add(file);
            }
        }
        return lista;
    }

    @Override
    public List<String> listFull(String... path) {

        FileList result = null;
        String dir=null;
        List<File> lista = new ArrayList<>();
        if (path.length>0)
            dir= getFileID(path[0]);
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        boolean ok = true;
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (path.length<=0) {
                    if (file != null && file.getParents() != null && file.getParents().contains(currID) && file.getMimeType()!=null &&file.getMimeType().contains("folder")) {
                        lista.add(file);
                        lista.addAll(pomListFull(file.getId(), lista));
                        //treba zameniti sa Listom i da se zove pomocna funkcija za ispis------
                    }
                    if (file!=null && file.getParents()!=null && file.getParents().contains(currID)&& file.getMimeType()!=null && !file.getMimeType().contains("folder")){
                        lista.add(file);
                    }
                } else {
                    if (file != null && file.getParents() != null && file.getParents().contains(dir) && file.getMimeType()!=null && file.getMimeType().contains("folder")) {
                        lista.add(file);
                        lista.addAll(pomListFull(file.getId(), lista));
                    }
                    if (file!=null && file.getParents()!=null && file.getParents().contains(dir)&&file.getMimeType()!=null &&!file.getMimeType().contains("folder")){
                        lista.add(file);
                    }
                }
            }
        }
        pomZaIspis(lista);
        // System.out.println(lista);
        List<String>fileStrings = new ArrayList<>();
        for (File file:lista){
            fileStrings.add(file.getName());
        }
        return fileStrings;
    }

    @Override
    public List<String> listExt(String... extname) {
        FileList result = null;
        List<File>lista = new ArrayList<>();
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (file!=null && file.getParents()!=null && file.getParents().contains(currID)){
                    String s = "."+extname[0];
                    if (file.getName().contains(s)){
                        lista.add(file);
                    }
                }
            }
        }
        pomZaIspis(lista);
        List<String>fileNames = new ArrayList<>();
        for (File file : lista){
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    @Override
    public List<String> listFilesWith(String substring) {
        FileList result = null;
        List<File>lista = new ArrayList<>();
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (file!=null && file.getParents()!=null && file.getParents().contains(currID)){
                    if (file.getName().contains(substring)){
                        lista.add(file);
                    }
                }
            }
        }
        pomZaIspis(lista);
        List<String>fileNames = new ArrayList<>();
        for (File file : lista){
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    @Override
    public boolean listContains(String path, String... fileNames) {
        String dirID = getFileID(path);
        FileList result = null;
        if (dirID == null) return false;
        boolean sadrzi = true;
        List<String>fajlovi = Arrays.stream(fileNames).collect(Collectors.toList());
        int brojacFajlova = 0;
        List<String>pomParents=new ArrayList<>();

        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        boolean ok = true;
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        }else{
            for(File file:files){
                if(file.getParents()!=null){
                    List<String>parents=new ArrayList<>();
                    parents.addAll(file.getParents());
                    if(parents.get(0).equalsIgnoreCase(dirID)){
                        if(fajlovi.contains(file.getName()))brojacFajlova++;
                    }
                }
            }
        }


        if (fajlovi.size()==brojacFajlova){
            System.out.println("Direktorijum: "+path +" sadrzi prosledjene fajlove.");
            return true;
        }
        System.out.println("Direktorijum: "+path +" NE sadrzi prosledjene fajlove.");

        return false;
    }

    @Override
    public String findDirecotry(String fileName) throws NoSuchFile {
        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File>files = result.getFiles();
        if (files==null || files.isEmpty()){
            System.out.println("Ne postoje fajlovi.");
        }else {
            for (File file : files){
                if (file.getName().equals(fileName)){
                    System.out.println(fileName + " pronadjen");
                    return fileName;
                }
            }
        }
        return fileName + " nije pronadjen";
    }

    @Override
    public List<String> sort(String order, String... criteria) {
        List<File> files = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        FileList result = null;
        if (order.equalsIgnoreCase("asc")){
            if (criteria.equals("date")){
                result = sortiranjePom("createdTime");
            }
            else {
                result = sortiranjePom("name");
            }
        } else if (order.equalsIgnoreCase("desc")){
            result = sortiranjePom("createdTime desc");
        } else result = sortiranjePom("name desc"); //neki generalni uslov ukoliko se ubaci sort

        files = result.getFiles();
        if (files==null || files.isEmpty()){
            System.out.println("Nema fajlova!");
            return null;
        }
        pomZaIspis(files);
        for (File f:files)
            fileNames.add(f.getName());
        return fileNames;
    }

    private FileList sortiranjePom(String order){
        FileList lista = null;
        try {
            lista = service.files().list().setQ("'"+getCurrID()+"' in parents").setOrderBy(order)
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, size)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<String> filesFromPeriod(String path, String time) {
        FileList result = null;
        File pom=null;
        String dir=null;
        List<String>zaRet=new ArrayList<>();

        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents, modifiedTime)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        boolean ok = true;
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        }
        else{
            for(File file: files){
                if(file.getName().equalsIgnoreCase(path))dir=file.getId();
            }
            if(dir==null){
                System.out.println("Lose uneta putanja");
                return null;
            }
            for(File file: files){
                List<String>parents=new ArrayList<>();
                parents=file.getParents();
                if(parents!=null&&parents.get(0).equalsIgnoreCase(dir)){
                    String[] datumi=time.split("-");

                    Date date1;
                    Date date2;

                    ArrayList<String>press=new ArrayList<>();
                    try {
                        date1=new SimpleDateFormat("dd/MM/yyyy").parse(datumi[0]);
                        date2=new SimpleDateFormat("dd/MM/yyyy").parse(datumi[1]);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    Date d;
                    try {
                        String[] s=file.getModifiedTime().toString().split("T");
                        d=new SimpleDateFormat("yyyy-MM-dd").parse((s[0]));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(d.after(date1)&&d.before(date2)) zaRet.add(file.getName());
                }
            }
        }
        return zaRet;
    }

    @Override
    public void fileInfoFilter(String... modifications) {
        int i = modifications.length-1;
        size = false;
        parent = false;
        date = false;

        for (int j=0;j<=i;j++){
            if (modifications[j].equalsIgnoreCase("name"))
                name = true;
            if (modifications[j].equalsIgnoreCase("size"))
                size = true;
            if (modifications[j].equalsIgnoreCase("parent"))
                parent = true;
            if (modifications[j].equalsIgnoreCase("date"))
                date = true;
        }
        System.out.println("Filteri uspesno podeseni.");
    }

    @Override
    public boolean forward(String path) {
        //path je ustvari ime
        //proci kroz svaki fajl koji se nalazi u getCurrID();
        //ako ne postoji folder s tim imenom onda vracas false i ne menjas CurrID
        // u suprotnom uradi setCurrID()
        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (file.getName().equals(path)) {
                    setCurrID(file.getId());
                    System.out.println("Trenutno si na: "+file.getName());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean backwards() {
        FileList result = null;
        File currDir = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                if (file.getId().equals(getCurrID())) {
                    currDir = file;
                    List<String> parents = currDir.getParents();
                    System.out.println(parents + "/" + currDir);
                    for (String s : parents){
                        setCurrID(s);
                        System.out.println("Vraceno unazad");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getRoot() {
        return null;
    }

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    public long getMaxStorageSize() {
        return maxStorageSize;
    }

    public void setMaxStorageSize(long maxStorageSize) {
        this.maxStorageSize = maxStorageSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public File getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(File storageFile) {
        this.storageFile = storageFile;
    }

    public File getMetadata() {
        return metadata;
    }

    public void setMetadata(File metadata) {
        this.metadata = metadata;
    }

    public HashMap<String, Integer> getDirectoryMap() {
        return directoryMap;
    }

    public void setDirectoryMap(HashMap<String, Integer> directoryMap) {
        this.directoryMap = directoryMap;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public Drive getService() {
        return service;
    }

    public void setService(Drive service) {
        this.service = service;
    }

    public String getCurrID() {
        return currID;
    }

    public void setCurrID(String currID) {
        this.currID = currID;
    }

    private boolean doesDirExist(String name) throws IOException {
        List<File> files = new ArrayList<File>();
        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, items(id, title)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals(name)){
                    return true;
                }
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return false;
    }

    public String getStoragesID() {
        return storagesID;
    }

    public void setStoragesID(String storagesID) {
        this.storagesID = storagesID;
    }

    public List<String> getRestrikcije() {
        return restrikcije;
    }

    public void setRestrikcije(List<String> restrikcije) {
        this.restrikcije = restrikcije;
    }

    public String getCurrentStorageID() {
        return currentStorageID;
    }

    public void setCurrentStorageID(String currentStorageID) {
        this.currentStorageID = currentStorageID;
    }

    public java.io.File getDatameta() {
        return datameta;
    }

    public void setDatameta(java.io.File datameta) {
        this.datameta = datameta;
    }
}