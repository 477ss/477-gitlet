package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

public class Stage implements Serializable {
    static final File COMMIT_FOLDER=GitletRepository.COMMIT_FOLDER;
    static final File HEAD_FILE=GitletRepository.HEAD_FILE;

    /** <file path, blob id> */
    HashMap<String, String> blob_list;
    LinkedList<String> rm_blob_list;

    public Stage(){
        blob_list=new HashMap<>();
        rm_blob_list=new LinkedList<>();
    }

    /** add to stage,
     * only one file at a time */
    public void add(Blob b){
        if(has_other_version(b) || !has_same_blob(b)){
            blob_list.put(b.path,b.id);
        }
        Head head =Utils.readObject(HEAD_FILE,Head.class);
        Commit commit=head.get_cur_commit();
        if(commit.has_same_blob(b)){
            blob_list.remove(b.path);
        }
        if(has_same_rm(b)){
            rm_blob_list.remove(b.id);
        }
    }

    /**add blob to rm_stage*/
    public void rm_add(Blob b){
        rm_blob_list.add(b.id);
    }

    public HashMap<String,String> get_stage_blob(){
        return blob_list;
    }

    public LinkedList<String> get_rm_blob(){
        return rm_blob_list;
    }

    public void clear(){
        blob_list.clear();
        rm_blob_list.clear();
    }

    public void save(){
        Utils.writeObject(GitletRepository.STAGE_FILE,this);
    }

    /** check if same blob in stage*/
    public Boolean has_same_blob(Blob b){
        return blob_list.containsValue(b.id);
    }

    /** check if other version in stage*/
    public Boolean has_other_version(Blob b){
        return blob_list.containsKey(b.path);
    }

    /** check if same blob in rm_stage*/
    public Boolean has_same_rm(Blob b){
        return rm_blob_list.contains(b.id);
    }

    /** print file name in stage */
    public void print(){
        for(String key:blob_list.keySet()){
            System.out.println(Utils.join(key).getName());
        }
    }

    /** print file name in rm_stage */
    public void rm_print(){
        for(String id:rm_blob_list){
            Blob b=GitletRepository.get_blob(id);
            System.out.println(b.name);
        }
    }

}
