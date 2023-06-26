package gitlet;

import java.io.File;
import java.io.Serializable;

public class Head implements Serializable {
    final static File REFS_HEADS_FOLDER=GitletRepository.REFS_HEADS_FOLDER;
    static final File HEAD_FILE=GitletRepository.HEAD_FILE;


    /** current ref file */
    File cur_ref_file;

    public Head(){
        cur_ref_file=GitletRepository.HEADS_MASTER_FILE;
    }

    /** get head commit of current branch */
    public Commit get_cur_commit(){
        Refs ref=get_cur_branch();
        return ref.get_cur_commit();
    }

    /** get current ref */
    public Refs get_cur_branch(){
        return Utils.readObject(cur_ref_file,Refs.class);
    }

    public void save(){
        Utils.writeObject(HEAD_FILE,this);
    }

}
