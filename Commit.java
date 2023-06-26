package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {
    /** date format */
    static final DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
    static final File COMMIT_FOLDER=GitletRepository.COMMIT_FOLDER;
    static final File INDEX_FILE=GitletRepository.INDEX_FILE;

    public String message;
    public String id;
    public Date date;
    public ArrayList<String> parent;
    public HashMap<String, String> blob_list;

    public Commit(String message,String parent,HashMap<String, String> blob_list){
        this.message=message;
        this.date=new Date();
        this.blob_list=blob_list;
        this.parent=new ArrayList<>(2);
        this.parent.add(parent);
        this.id=Utils.sha1(dateFormat.format(date), message, parent, blob_list.toString());
    }

    /** init */
    public Commit(){
        this.message="initial commit";
        this.parent=new ArrayList<>();
        this.date=new Date(0);
        this.blob_list=new HashMap<>();
        this.id=Utils.sha1(dateFormat.format(date), message, parent.toString(), blob_list.toString());
    }

    public void save(){
        File f=Utils.join(COMMIT_FOLDER,id);
        Utils.writeObject(f,this);
        Index index=Utils.readObject(INDEX_FILE, Index.class);
        index.put_commit(id,f);
        index.save();
    }

    /** check if same blob in commit*/
    public Boolean has_same_blob(Blob b){
        return blob_list.containsValue(b.id);
    }

    @Override
    public String toString(){
        if(parent.size()==2){
            return String.format("===\ncommit %s\nMerge: %.7s %.7s\nDate: %s\n%s\n\n",id,parent.get(0),parent.get(1),dateFormat.format(date),message);
        }else{
            return String.format("===\ncommit %s\nDate: %s\n%s\n\n",id,dateFormat.format(date),message);
        }
    }

}
