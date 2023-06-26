package gitlet;

import java.io.File;
import java.io.Serializable;

public class Refs implements Serializable {
    final static File COMMIT_FOLDER=GitletRepository.COMMIT_FOLDER;
    static final File REFS_HEADS_FOLDER=GitletRepository.REFS_HEADS_FOLDER;

    /** current commit id of this refs */
    public String cur_commit_id;
    public String ref_name;

    public Refs(String cur_commit_id, String ref_name){
        this.cur_commit_id=cur_commit_id;
        this.ref_name=ref_name;
    }

    public Commit get_cur_commit(){
        Index index=Utils.readObject(GitletRepository.INDEX_FILE, Index.class);
        return index.get_commit(cur_commit_id);
    }

    /** update head commit*/
    public void update_cur_commit(Commit commit){
        cur_commit_id=commit.id;
    }

    public void save(){
        File f=Utils.join(REFS_HEADS_FOLDER,ref_name);
        Utils.writeObject(f,this);
    }

    public Boolean equals(Refs ref){
        return ref.ref_name.equals(ref_name) && cur_commit_id.equals(ref.cur_commit_id);
    }

}
