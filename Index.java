package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class Index implements Serializable {
    static final File INDEX_FILE=GitletRepository.INDEX_FILE;
    static final File COMMIT_FOLDER=GitletRepository.COMMIT_FOLDER;
    /**id前六位*/
    public HashMap<String, File> commit_index;
    public HashMap<String, File> blob_index;

    public Index(){
        commit_index=new HashMap<>();
        blob_index=new HashMap<>();
    }

    public Commit get_commit(String id){
       File f=commit_index.get(String.format("%.6s",id));
       if(f==null){
           return null;
       }
       return Utils.readObject(f, Commit.class);
    }

    public Blob get_blob(String id){
        File f=blob_index.get(String.format("%.6s",id));
        if(f==null){
            return null;
        }
        return Utils.readObject(f, Blob.class);
    }

    public void put_commit(String id, File f){
        commit_index.put(String.format("%.6s",id),f);
    }

    public void put_commit(String id){
        File f=Utils.join(COMMIT_FOLDER,id);
        commit_index.put(String.format("%.6s",id),f);
    }

    public void put_blob(String id, File f){
        blob_index.put(String.format("%.6s",id),f);
    }

    public void save(){
        Utils.writeObject(INDEX_FILE,this);
    }

    public Boolean has_commit(String id){
        return commit_index.containsKey(String.format("%.6s",id));
    }

    public Boolean has_blob(String id){
        return blob_index.containsKey(String.format("%.6s",id));
    }
}
