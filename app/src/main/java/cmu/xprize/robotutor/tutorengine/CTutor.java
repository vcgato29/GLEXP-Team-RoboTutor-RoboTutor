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

package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.util.CPreferenceCache;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.scene_initializer;
import cmu.xprize.robotutor.tutorengine.graph.type_action;
import cmu.xprize.robotutor.tutorengine.graph.type_timer;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;


/**
 *  Each Tutor instance is represented by a CTutor
 *
 */
public class CTutor implements ILoadableObject2 {

    private boolean traceMode = false;

    // This is the local tutor scope in which all top level objects and variables are defined
    // May have child scopes for local variables -

    private TScope                        mTutorScope;
    private CMediaManager                 mMediaManager;

    private HashMap<String, ITutorScene>  mScenes  = new HashMap<String, ITutorScene>();
    private HashMap<String, ITutorObject> mObjects = new HashMap<String, ITutorObject>();

    private ArrayList<String>            fFeatures = new ArrayList<String>();
    private ArrayList<String>            fDefaults = new ArrayList<String>();

    public Context                       mContext;
    public ITutorLogManager              mTutorLogManager;
    public ITutorGraph mTutorGraph;
    public CSceneGraph                   mSceneGraph;
    public ITutorManager                 mTutorContainer;
    public ViewGroup                     mSceneContainer;

    public String                        mTutorName;
    public AssetManager                  mAssetManager;
    public boolean                       mTutorActive = false;

    private int                                 _framendx = 0;
    private HashMap<String, scene_initializer>  _sceneMap = new HashMap<String, scene_initializer>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // json loadable
    public scene_initializer[]           scenedata;
    public String                        language;
    public String                        navigatorType;
    public HashMap<String,CMediaPackage> soundMap;

    public String engineLanguage;

    private int index = 0;  // test debug

    static private final String  TAG   = LayoutInflater.class.getSimpleName();
    static private final boolean DEBUG = false;



    public CTutor(Context context, String name, ITutorManager tutorContainer, ITutorLogManager logManager, TScope rootScope, String tarLanguage, String featSet) {

        mTutorScope      = new TScope(this, name, rootScope);
        mContext         = context;
        mTutorName       = name;
        mTutorContainer  = tutorContainer;
        mTutorLogManager = logManager;

        mAssetManager    = context.getAssets();
        mMediaManager    = CMediaManager.getInstance();

        setTutorFeatures(featSet);

        // Update the unique instance string for the tutor
        //
        CPreferenceCache.updateTutorInstance(name);

        // Configure the initial language based on the engine_descriptor JSON setting -
        // tutor may override if "language" is set in tutor_description JSON image
        //
        mMediaManager.setLanguageFeature(this, tarLanguage);

        inflateTutor();
    }


    /**
     *
     */
    public void onDestroy() {
    }


    private void inflateTutor() {

        // Load the "tutor_descriptor.json" file
        loadTutorFactory();
        loadSceneNavigator();
    }


    public class Queue implements Runnable {

        protected final String _command;

        public Queue(String command) {
            _command = command;
        }

        @Override
        public void run() {

            switch(_command) {
                case TCONST.ENDTUTOR:
                    endTutor();
                    break;


            }
        }
    }


    /**
     * Post a command to the scenegraph queue
     *
     * @param command
     */
    public void post(String command) {

        mainHandler.post(new Queue(command));
    }


    private void loadSceneNavigator() {

        switch(navigatorType) {
            case TCONST.SIMPLENAV:
                mTutorGraph = new CTutorGraph(this, mTutorName, mTutorContainer, mTutorScope);
                break;

            case TCONST.GRAPHNAV:
                //mTutorGraph = new CSceneGraphNavigator(mTutorName);
                break;
        }

        mTutorGraph.initTutorContainer(mTutorContainer);
        mSceneGraph = mTutorGraph.getAnimator();
    }


    public void setSceneContainer(ViewGroup container) {
        mSceneContainer = container;
    }


    /**
     * Return the view within the current scene container
     *
     * @param findme
     * @return
     */
    public View getViewByName(String findme) {

        HashMap map = mTutorGraph.getChildMap();

        return (View)map.get(findme);
    }



    public ITutorObject getViewById(int findme, ViewGroup container) {
        ITutorObject foundView = null;

        if(container == null)
            container = (ViewGroup)mSceneContainer;

        try {
            for (int i = 0; (foundView == null) && (i < container.getChildCount()); ++i) {

                ITutorObject nextChild = (ITutorObject) container.getChildAt(i);

                if (((View) nextChild).getId() == findme) {
                    foundView = nextChild;
                    break;
                } else {
                    if (nextChild instanceof ViewGroup)
                        foundView = getViewById(findme, (ViewGroup) nextChild);
                }
            }
        }
        catch (Exception e) {
            Log.i(TAG, "View walk error: " + e);
        }
        return foundView;
    }


    public TScope getScope() {
        return mTutorScope;
    }


    /**
     * This is where the tutor gets kick started
     */
    public void launchTutor() {
        mTutorActive = true;
        mTutorGraph.post(TCONST.FIRST_SCENE);
    }


    /**
     * This is where the tutor stops
     */
    public void endTutor() {

        // Only ever pop once per tutor
        // TODO: this will change - at the moment may be called multiple times during shutdown.
        // e.g. from a stream flow.

        if(mTutorActive) {
            mTutorActive = false;

            mTutorContainer.popView(false, null);
            mTutorGraph.onDestroy();

            CTutorEngine.killTutor(mTutorName);
        }
    }


    public ITutorGraph getTutorGraph() {
        return mTutorGraph;
    }

    public CSceneGraph getSceneGraph() {
        return mSceneGraph;
    }


    //**************************************************************************
    // Language management

    // The language ID is also used as a feature to permit conditioning on language
    // within scripts.
    //
    public void updateLanguageFeature(String langFtr) {

        // Remove any active language - Only want one language feature active
        setDelFeature(CMediaManager.getLanguageFeature(this));

        setAddFeature(langFtr);
    }

    // Language management
    //**************************************************************************


    /**
     * This provides sceneGraph nodes access to the Assetmanager through their scopes
     * mTutor field
     *
     * @param path
     * @return
     * @throws IOException
     */
    public InputStream openAsset(String path) throws IOException {
        return mAssetManager.open(path);
    }


    // framendx is a simple counter used it uniquely id a scene instance for logging
    //
    public void incFrameNdx() {
        _framendx++;
    }


    /** Global logging support - each scene instance and subscene animation instance represent
    *                          object instances in the log.
    *                          The frameid is a '.' delimited string representing the:
    *
    *     framendx:graphnode.nodemodule.moduleelement... :animationnode.animationelement...iterationNdx
    *
    * 			Semantics - each ':' represents the root of a new different (sub)graph
    *   e.g.
    *
    * 	  000001:root.start.SstartSplash...:root.Q0A.CSSbSRule1Part1AS...
    */
    private String constructLogName(String attr) {
        String attrName = "L00000";
        String frame;

        frame = Integer.toString(_framendx);

        // Note: name here is the scene name itself which is the context in which we are executing

        //attrName = attrName.slice(0, 6-frame.length) + frame + "_" + name +"_" + attr + "_" + gTutor.gNavigator.iteration.toString();

        //attrName = name +"_" + attr + "_" + gTutor.gNavigator.iteration.toString();

        return attrName;
    }


    public void add(String Id, ITutorObject obj) {

        mObjects.put(Id, obj);
    }


    public ITutorObject get(String Id) {

        return mObjects.get(Id);
    }


    /**
     *  Scene Creation / Destruction
     *
     * @param scenedata
     * @return
     */
    public View instantiateScene(scene_descriptor scenedata) {

        int i1;
        View tarScene;
        View subScene;

        int id = mContext.getResources().getIdentifier(scenedata.id, "layout", mContext.getPackageName());

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        tarScene = inflater.inflate(id, null );

        if(traceMode) Log.d(TAG, "Creating Scene : " + scenedata.id);

        tarScene.setVisibility(View.VISIBLE);

//        mTutorContainer.addView(tarScene, index);
//        mTutorContainer.setDisplayedChild(index++);

        // Generate the automation hooks
        automateScene((ITutorSceneImpl) tarScene, scenedata);

        // Parse the JSON onCreate spec data
        onCreate(scenedata);

        return (View) tarScene;
    }


    private void automateScene(ITutorSceneImpl tutorContainer, scene_descriptor scenedata) {

        // Propogate to children
        //
        HashMap childMap = new HashMap();

        // Record each SCENE Object
        //
        scenedata.instance = tutorContainer;
        scenedata.children = childMap;

        tutorContainer.setParent(mTutorContainer);
        tutorContainer.setTutor(this);
        tutorContainer.setNavigator(mTutorGraph);
        tutorContainer.setLogManager(mTutorLogManager);

        mapChildren(tutorContainer, childMap);

        Iterator<?> tObjects = childMap.entrySet().iterator();

        // post create / inflate / init / map - here everything is created including the
        // view map to permit findViewByName
        //
        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            ((ITutorObject)(entry.getValue())).postInflate();
        }
    }


    private void mapChildren(ITutorSceneImpl tutorContainer, HashMap childMap) {

        ITutorObject child;

        int count = ((ViewGroup) tutorContainer).getChildCount();

        // Iterate through all children
        for (int i = 0; i < count; i++) {
            try {
                child = (ITutorObject) ((ViewGroup) tutorContainer).getChildAt(i);

                if(childMap.containsKey(child.name())) {
                    Log.e(TAG, "ERROR: Duplicate child view in:" + tutorContainer.name());
                    System.exit(1);
                }

                childMap.put(child.name(), child);

                child.setParent(tutorContainer);
                child.setTutor(this);
                child.setNavigator(mTutorGraph);
                child.setLogManager(mTutorLogManager);

                if(child instanceof ITutorSceneImpl) {
                    mapChildren((ITutorSceneImpl)child, childMap);
                }

            } catch (ClassCastException e) {
                Log.e(TAG, "ERROR: Non-ITutor child view in:" + tutorContainer.name());
                System.exit(1);
            }
        }
    }


    private void onCreate(scene_descriptor scenedata) {

        // Parse the oncreate command set

        type_action[] createCmds    = _sceneMap.get(scenedata.id).oncreate;

        // Can have an empty JSON array - so filter that out
        if(createCmds != null) {

            for (type_action cmd : createCmds) {
                cmd.applyNode();
            }
        }
    }


    /**
     * generate the working feature set for this tutor instance
     *
     * @param featSet
     */
    public void setTutorFeatures(String featSet) {
        List<String> featArray = new ArrayList<String>();

        if(featSet != null && featSet.length() > 0)
            featArray = Arrays.asList(featSet.split(":"));

        fFeatures = new ArrayList<String>();

        // Add default features 

        for (String feature : fDefaults)
        {
            fFeatures.add(feature);
        }

        // Add instance feature

        for (String feature : featArray) {
            fFeatures.add(feature);
        }
    }


    /**
     *  get : delimited string of features
     * ## Mod Oct 16 2012 - logging support
     *
     */
    public String getFeatures() {
        StringBuilder builder = new StringBuilder();

        for(String feature: fFeatures) {
            builder.append(feature).append(':');
        }
        builder.deleteCharAt(builder.length());

        return builder.toString();
    }


    /**
     * set : delimited string of features
     * ## Mod Dec 03 2013 - DB state support
     *
     * @param ftrSet
     */
    public void setFeatures(String ftrSet) {
        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(ftrSet.split(","));

        fFeatures.clear();
        
        for (String feature : featArray) {
            fFeatures.add(feature);
        }
    }


    // udpate the working feature set for this instance
    //
    public void setAddFeature(String feature)
    {
        // Add new features - no duplicates

        if(fFeatures.indexOf(feature) == -1)
        {
            fFeatures.add(feature);
        }
    }


    // udpate the working feature set for this instance
    //
    public void setDelFeature(String feature) {
        int fIndex;

        // remove features - no duplicates

        if((fIndex = fFeatures.indexOf(feature)) != -1)
        {
            fFeatures.remove(fIndex);
        }
    }


    //## Mod Jul 01 2012 - Support for NOT operation on features.
    //
    //	
    private boolean testFeature(String element)
    {
        if(element.charAt(0) == '!')
        {
            return (fFeatures.indexOf(element.substring(1)) != -1)? false:true;
        }
        else {
            return (fFeatures.indexOf(element) != -1) ? true : false;
        }
    }


    // test possibly compound features
    //
    public boolean testFeatureSet(String featSet) {
        List<String> disjFeat = Arrays.asList(featSet.split("\\|"));   // | Disjunctive features
        List<String> conjFeat;                                          // & Conjunctive features

        // match a null set - i.e. empty string means the object is not feature constrained

        if(featSet.equals(""))
                    return true;

        // Check all disjunctive featuresets - one in each element of disjFeat
        // As long as one is true we pass

        for (String dfeature : disjFeat)
        {
            conjFeat = Arrays.asList(dfeature.split("\\&"));

            // Check that all conjunctive features are set in fFeatures 

            for (String cfeature : conjFeat) {
                if(testFeature(cfeature))
                                return true;
            }
        }
        return false;
    }


    public String getTutorName() {
        return mTutorName;
    }


    public AssetManager getAssetManager() {
        return mAssetManager;
    }


    // Scriptable graph next command
    public void eventNext() {
        mSceneGraph.post(TCONST.NEXT_NODE);
    }

    // Scriptable graph goto command
    public void gotoNode(String nodeID) {
        mSceneGraph.post(TCONST.GOTO_NODE, nodeID);
    }



    //************ Serialization


    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/tutor_descriptor.json
     *
     * Note that this is a stopgap until we can replace the Android view inflation mechanism
     * and completely define view layout in TDESC
     */
    private void loadTutorFactory() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutorName + "/" + TCONST.TDESC)), (IScope2)mTutorScope);

        } catch (JSONException e) {
            Log.d(TAG, "error");
        }
    }


    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

        // Use updateLanguageFeature to properly override the Engine language feature
        if(language != null)
            mMediaManager.setLanguageFeature(this, language);

        // push the soundMap into the MediaManager -
        //
        mMediaManager.setMediaPackage(this, soundMap);

        // Create a associative cache for the initialization data
        //
        for(scene_initializer scene : scenedata) {
            _sceneMap.put(scene.id, scene);
        }
    }
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
