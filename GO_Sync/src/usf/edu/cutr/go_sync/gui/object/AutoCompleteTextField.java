/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package usf.edu.cutr.go_sync.gui.object;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author megordon
 */
public class AutoCompleteTextField extends JTextField {

    public List list;
    public boolean isCaseSensitive;
    public boolean isStrict;
    private ArrayList<String> matches;
    private int matchIndex = 0;
    
    class AutoDoc extends PlainDocument {

        @Override
        public void replace(int i, int j, String s, AttributeSet attributeset) throws BadLocationException {
            super.remove(i, j);
            insertString(i, s, attributeset);
        }

        @Override
        public void insertString(int i, String s, AttributeSet attributeset) throws BadLocationException {
            if (s == null || "".equals(s)) {
                return;
            }
            String s1 = getText(0, i);
            //String s2 = findMatch(s1 + s);
            matches = findAllMatches(s1 + s);
            String s2 = matches.get(matchIndex);
            int j = (i + s.length()) - 1;
            if (isStrict && s2 == null) {
                s2 = findMatch(s1);
                j--;
            } else if (!isStrict && s2 == null) {
                super.insertString(i, s, attributeset);
                return;
            }
            super.remove(0, getLength());
            super.insertString(0, s2, attributeset);
            setSelectionStart(j + 1);
            setSelectionEnd(getLength());
        }
        
        public void insertString(String s) throws BadLocationException {
            int i = Math.min(getCaret().getDot(), getCaret().getMark());
            super.remove(0, getLength());
            super.insertString(0, s, null);

            setSelectionStart(i);
            setSelectionEnd(getLength());
        }

        @Override
        public void remove(int i, int j) throws BadLocationException {
            int k = getSelectionStart();
            if (k > 0) {
                k--;
            }
            String s = findMatch(getText(0, k));
            if (!isStrict && s == null) {
                super.remove(i, j);
            } else {
                super.remove(0, getLength());
                super.insertString(0, s, null);
            }
            try {
                setSelectionStart(k);
                setSelectionEnd(getLength());
            } catch (Exception exception) {
            }
        }
    }
    
    public AutoCompleteTextField(List _list) {
        isCaseSensitive = false;
        isStrict = false;
        if (_list == null) {
            throw new IllegalArgumentException("An AutoCompleteTextField must have a list!");
        } else {
            list = _list;
            init();
            return;
        }
    }
    
    private void init() {
        setDocument(new AutoDoc());
        if(isStrict && list.size() > 0) {
            setText(list.get(0).toString());
        }
        
        KeyListener listener = new KeyListener() {

            public void keyTyped(KeyEvent e) {
//                 System.out.println("Typed!");
            }

            public void keyPressed(KeyEvent e) {
//                System.out.println("Pressed!");
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (matches != null && matches.size() > 1) {
                        matchIndex++;
                        if (matchIndex >= matches.size()) {
                            matchIndex = 0;
                        }
                        changeMatch();
                    } else {
                        matchIndex = 0;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (matches != null && matches.size() > 1) {
                        matchIndex--;
                        if (matchIndex < 0) {
                            matchIndex = matches.size() - 1;
                        }
                        changeMatch();
                    } else {
                        matchIndex = 0;
                    }
                } else {
                    matchIndex = 0;
                }
            }
        };
        this.addKeyListener(listener);
    }
    
    private void changeMatch() {
        AutoDoc doc = (AutoDoc) getDocument();
        if (doc != null) {
            try {
                doc.insertString(matches.get(matchIndex).toString());
            } catch (Exception ex) {
                System.err.println("Problem inserting string..." + ex.getLocalizedMessage());
            }
        }
    }
    
    private String findMatch(String s1) {
        String s2;
        for(Object i : list) {
            if(i != null && i.getClass().getName().contains("String")) {
                s2 = (String) i;
                if(!isCaseSensitive && s2.toLowerCase().startsWith(s1.toLowerCase())) {
                    return s2;
                } else if(isCaseSensitive && s2.startsWith(s1)) {
                    return s2;
                }   
            }
        }
        return null;
    }

        private ArrayList<String> findAllMatches(String s1) {
        String s2;
        ArrayList<String> m = new ArrayList<String>();
        for(Object i : list) {
            if(i != null && i.getClass().getName().contains("String")) {
                s2 = (String) i;
                if(!isCaseSensitive && s2.toLowerCase().startsWith(s1.toLowerCase())) {
                    m.add(s2);
                } else if(isCaseSensitive && s2.startsWith(s1)) {
                    m.add(s2);
                }   
            }
        }
        return m;
    }

    @Override
    public void replaceSelection(String s) {
        AutoDoc doc = (AutoDoc) getDocument();
        if (doc != null) {
            try {
                int i = Math.min(getCaret().getDot(), getCaret().getMark());
                int j = Math.max(getCaret().getDot(), getCaret().getMark());
                doc.replace(i, j - i, s, null);
            } catch (Exception ex) {
                System.err.println("Problem inserting string: " + ex.getLocalizedMessage());
            }
        }
    }
}
