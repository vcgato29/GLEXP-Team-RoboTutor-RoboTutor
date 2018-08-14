package cmu.xprize.comp_nd.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cmu.xprize.comp_nd.CNd_Component;
import cmu.xprize.comp_nd.R;
import cmu.xprize.util.MathUtil;

import static cmu.xprize.comp_nd.ND_CONST.HUN_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.LEFT_NUM;
import static cmu.xprize.comp_nd.ND_CONST.MAX_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.MAX_ONES;
import static cmu.xprize.comp_nd.ND_CONST.MAX_TENS;
import static cmu.xprize.comp_nd.ND_CONST.NO_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.ONE_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.RIGHT_NUM;
import static cmu.xprize.comp_nd.ND_CONST.TEN_DIGIT;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;

/**
 * CNd_LayoutManager_BaseTen
 *
 * Uses a Constraint Layout (nd_layout) to display BaseTen blocks as scaffolding.
 *
 * <p>
 * Created by kevindeland on 7/17/18.
 */

public class CNd_LayoutManager_BaseTen implements CNd_LayoutManagerInterface {

    CNd_Component _component;
    Context _context;

    public CNd_LayoutManager_BaseTen(CNd_Component component, Context context) {
        this._component = component;
        this._context = context;
    }


    @Override
    public void initialize() {
        CNd_Component.inflate(_context, R.layout.nd_layout_3, _component);

        // ND_SCAFFOLD_TODO move this to scaffolding
        setDebugButtonBehavior();
    }

    @Override
    public void resetView() {
        _component.setVisibility(CNd_Component.GONE);
    }

    @Override
    public void displayDigits(int left, int right) {

        // set left number
        ((TextView) _component.findViewById(R.id.symbol_left_hun)).setText(String.valueOf(getHunsDigit(left)));
        ((TextView) _component.findViewById(R.id.symbol_left_ten)).setText(String.valueOf(getTensDigit(left)));
        ((TextView) _component.findViewById(R.id.symbol_left_one)).setText(String.valueOf(getOnesDigit(left)));

        // hide digits not used
        if (getHunsDigit(left) == 0) {
            _component.findViewById(R.id.symbol_left_hun).setVisibility(View.GONE);
            _component.findViewById(R.id.left_hun_container).setVisibility(View.GONE);

            if (getTensDigit(left) == 0) {
                _component.findViewById(R.id.symbol_left_ten).setVisibility(View.GONE);
                _component.findViewById(R.id.left_ten_container).setVisibility(View.GONE);
            }
        }

        // set right number
        ((TextView) _component.findViewById(R.id.symbol_right_hun)).setText(String.valueOf(getHunsDigit(right)));
        ((TextView) _component.findViewById(R.id.symbol_right_ten)).setText(String.valueOf(getTensDigit(right)));
        ((TextView) _component.findViewById(R.id.symbol_right_one)).setText(String.valueOf(getOnesDigit(right)));

        // hide digits not used
        if (getHunsDigit(right) == 0) {
            _component.findViewById(R.id.symbol_right_hun).setVisibility(View.GONE);
            _component.findViewById(R.id.right_hun_container).setVisibility(View.GONE);

            if (getTensDigit(right) == 0) {
                _component.findViewById(R.id.symbol_right_ten).setVisibility(View.GONE);
                _component.findViewById(R.id.right_ten_container).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void displayConcreteRepresentations(int left, int right) {

        displayConcrete("left", left);
        displayConcrete("right", right);
    }

    /**
     * Display concrete number representations
     * @param numberLoc either left or right.
     * @param numberValue number value to display
     */
    private void displayConcrete(String numberLoc, int numberValue) {


        ImageView imgView;

        int hunsDigit, tensDigit, onesDigit;


        hunsDigit = getHunsDigit(numberValue);
        for (int i=1; i <= MAX_HUNS; i++) {

            imgView = getBaseTenConcreteUnitView(numberLoc, "hun", i);
            imgView.setVisibility( i <= hunsDigit ? View.VISIBLE : View.GONE); // only show first N, N=hunsDigit
        }


        tensDigit = getTensDigit(numberValue);
        for(int i = 1; i <= MAX_TENS; i++) {

            imgView = getBaseTenConcreteUnitView(numberLoc, "ten", i);
            imgView.setVisibility( i <= tensDigit ? View.VISIBLE : View.GONE); // only show first N, N=tensDigit
        }

        onesDigit = getOnesDigit(numberValue);
        for (int i = 1; i <= MAX_ONES; i++) {
            imgView = getBaseTenConcreteUnitView(numberLoc, "one", i);
            imgView.setVisibility( i <= onesDigit ? View.VISIBLE : View.GONE); // only show first N, N=onesDigit
        }

    }

    // ------------
    // VIEW HELPER
    // ------------
    /**
     * Helper function to return a View of a Concrete Representation by its id.
     *
     * "top_ten_1" will be the first ten in the top number
     * or "left_hun_4" will be the 4th hundred in the left number
     *
     * @param numberLoc top, bottom, left, right, etc
     * @param digit one, ten, hun
     * @param value the index of the thing
     * @return The ImageView of the concrete representation
     */
    private ImageView getBaseTenConcreteUnitView(String numberLoc, String digit, int value) {

        String viewId = numberLoc + "_" + digit + "_" + value;
        int resID = _context.getResources().getIdentifier(viewId, "id", _context.getPackageName());

        return (ImageView) _component.findViewById(resID);
    }


    /**
     * Returns a TextView displaying a digit.
     *
     * @param numberLoc top, bottom, left, right, etc
     * @param digit one, ten, hun
     * @return The TextView of the digit
     */
    private TextView getBaseTenDigitView(String numberLoc, String digit) {

        String viewId = "symbol_" + numberLoc + "_" + digit;
        int resID = _component.getResources().getIdentifier(viewId, "id", _context.getPackageName());

        return (TextView) _component.findViewById(resID);
    }


    @Override
    public void enableChooseNumber(boolean enable) {

        Log.wtf("CHOOSE_ME", "y u no work?");

        View chooseLeft = _component.findViewById(R.id.symbol_left_num);
        chooseLeft.setOnClickListener(enable ? new ChooseListener("left"): null);

        View chooseRight = _component.findViewById(R.id.symbol_right_num);
        chooseRight.setOnClickListener(enable ? new ChooseListener("right"): null);
    }

    /**
     * private ClickListener which sends student response back the the Component.
     */
    private class ChooseListener implements View.OnClickListener {

        String _choice; // "left" or "right"
        ChooseListener(String choice) {
            this._choice = choice;
        }

        @Override
        public void onClick(View view) {
            _component.registerStudentChoice(_choice);
            _component.applyBehavior("SELECT_ANSWER_EVENT");
        }
    }

    @Override
    public void highlightDigit(String digit) {
        highlightUnits(LEFT_NUM, digit, true);
        highlightUnits(RIGHT_NUM, digit, true);
    }


    // highlight Units
    private void highlightUnits(String numberLoc, String digitToHighlight, boolean suppressOthers) {

        // cycle through each digit
        String[] allDigits = {ONE_DIGIT, TEN_DIGIT, HUN_DIGIT};

        for (String d : allDigits) {

            int colorToSet = d.equals(digitToHighlight) ? _context.getResources().getColor(R.color.ndHighlight) : Color.TRANSPARENT;


            TextView symbolicRep = getBaseTenDigitView(numberLoc, d);
            symbolicRep.setBackgroundColor(colorToSet);

            String boxId = numberLoc + "_" + d + "_container";
            Log.d("HIGHLIGHT", boxId);
            int resID = _context.getResources().getIdentifier(boxId, "id", _context.getPackageName());
            _component.findViewById(resID).setBackgroundColor(colorToSet);
        }


    }


    private String currentHighlight;

    private void setDebugButtonBehavior() {
        _component.findViewById(R.id.debug_nd_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentHighlight == null) {
                    highlightDigit(HUN_DIGIT);
                    currentHighlight = HUN_DIGIT;
                } else {
                    switch(currentHighlight) {

                        case HUN_DIGIT:
                            highlightDigit(TEN_DIGIT);
                            currentHighlight = TEN_DIGIT;
                            break;

                        case TEN_DIGIT:
                            highlightDigit(ONE_DIGIT);
                            currentHighlight = ONE_DIGIT;
                            break;

                        case ONE_DIGIT:
                            highlightDigit(NO_DIGIT);
                            currentHighlight = null;
                            break;

                    }
                }
            }
        });
    }
}
