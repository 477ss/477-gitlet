package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    static final File BLOB_FOLDER=GitletRepository.BLOB_FOLDER;
    static final File INDEX_FILE=GitletRepository.INDEX_FILE;

    public String name;
    public byte[] content;
    public String id;
    public String path;

    public Blob(String name, byte[] content, String file_path){
        this.name=name;
        this.content=content;
        this.path =file_path;
        this.id=Utils.sha1(path,content);
    }

    public void save(){
        File f=Utils.join(BLOB_FOLDER,id);
        Utils.writeObject(f,this);
        Index index=Utils.readObject(INDEX_FILE, Index.class);
        index.put_blob(id,f);
        index.save();
    }



}