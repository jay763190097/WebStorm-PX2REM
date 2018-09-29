package com.jay.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.jay.constvalue.ConstValue;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * @author 01375894
 * date: 2018/09/29
 *
 */
public class Px2RemAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        //PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        //获取idea选中区域数字
        String s = selectionModel.getSelectedText();

        if(s == null || "".equals(s)){
            CaretModel caretModel = editor.getCaretModel();
            int caretOffset = caretModel.getOffset();  //光标所在位置
            int lineNum = document.getLineNumber(caretOffset);
            int lineStartOffset = document.getLineStartOffset(lineNum);
            int lineEndOffset = document.getLineEndOffset(lineNum);

            //整行内容
            String lineContent = document.getText(new TextRange(lineStartOffset, caretOffset));
            int formatStart = lineContent.lastIndexOf(":")+1;
            if(formatStart == 0 || !lineContent.contains("px"))
                return;
            String formatText = lineContent.substring(formatStart);
            if(!formatText.contains("px"))
                return;
            formatText = formatText.replace(";","");
            try{
                String results = StringUtils.join(
                        Arrays.stream(formatText.split(" "))
                                .map((ele)->{
                                    if(!ele.contains("px") || "".equals(ele))
                                        return ele.trim();
                                    double rem;
                                    double px;
                                    px = Double.valueOf(ele.substring(0, ele.lastIndexOf("px")).trim());
                                    rem = px / ConstValue.remBaseValue;
                                    String txt;
                                    if(ConstValue.showNote) {
                                        txt = String.format("%.2f", rem).trim() + "rem /* "+px+"/"+ConstValue.remBaseValue + " */";
                                    } else {
                                        txt = String.format("%.2f", rem).trim() + "rem";
                                    }
                                    return txt;
                                })
                                .toArray(), " ");

                WriteCommandAction.runWriteCommandAction(project, () ->
                        document.replaceString(lineStartOffset+formatStart, caretOffset, results)
                );
            } catch (Exception ex){
                return;
            }
            return;
        }

        int index = s.indexOf("px");
        if(index > -1) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                double px = Double.valueOf(s.substring(0, index));
                double rem = px / ConstValue.remBaseValue;
                if(ConstValue.showNote) {
                    document.replaceString(start, end, String.format("%.2f", rem) + "rem /* "+px+"/"+ConstValue.remBaseValue + " */");
                } else {
                    document.replaceString(start, end, String.format("%.2f", rem) + "rem");
                }
            });
        }
    }
}
