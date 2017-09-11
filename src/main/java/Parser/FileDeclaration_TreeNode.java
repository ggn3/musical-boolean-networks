package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

public class FileDeclaration_TreeNode extends MutableTreeNodeImpl{
    CodeType codeType;

    public FileDeclaration_TreeNode(CodeType ct){
        this.codeType = ct;
    }

   public CodeType getCodeType() {
        return codeType;
    }
}

