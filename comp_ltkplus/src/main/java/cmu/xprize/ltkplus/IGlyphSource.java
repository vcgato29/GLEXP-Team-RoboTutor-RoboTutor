//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.ltkplus;


import android.graphics.Paint;
import android.graphics.Rect;

public interface IGlyphSource {

    public void recCallBack(CRecResult[] _ltkCandidates, CRecResult[] _ltkPlusCandidates, int sampleIndex);


    public String       getExpectedChar();

    public CGlyph       getGlyph();

    public Rect         getViewBnds();

    public Rect         getFontBnds();

    public Rect         getFontCharBounds(String candChar);

    public float        getBaseLine();

    public float        getDotSize();

    public Paint        getPaint();
}
