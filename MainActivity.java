package com.example.raghav5gupta.test;


import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Sampler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.MenuItem;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.analysis.MeasuringTool;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Unit;
import com.esri.core.map.bing.Result;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    static GeodatabaseSyncTask gdbSyncTask;
    static final String url = "http://services6.arcgis.com/8uhZEDx5sMp4z8Dd/arcgis/rest/services/example/FeatureServer";
    static ProgressDialog progress;
    static final String DEFAULT_GDB_PATH = "/ArcGIS/samples/OfflineEditor/";
    private static String gdbFileName = Environment.getExternalStorageDirectory().getPath() + DEFAULT_GDB_PATH + "offlinedata.geodatabase";
    MapView mMapView = null;
    LocationDisplayManager LocationDisplay;
    GraphicsLayer mLocationLayer;
    SimpleFillSymbol fillSymbol;
    Unit[] linearUnits = new Unit[]{

            Unit.create(LinearUnit.Code.METER),
    };



    public class ConnectToServer extends AsyncTask<String,Void,Void>{
        @Override
        protected void onPreExecute() {
            //progress = new ProgressDialog(MainActivity.this);
            progress = ProgressDialog.show(MainActivity.this, "GDB Downloading",
                    "Processing... Please wait...");

        }
        @Override
        protected Void doInBackground(String... params) {

            gdbSyncTask = new GeodatabaseSyncTask(url, null);
            gdbSyncTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

                @Override
                public void onError(Throwable arg0) {
                    Toast.makeText(MainActivity.this,"Error in Fetching featureData",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCallback(FeatureServiceInfo fsinfo) {
                    if (fsinfo.isSyncEnabled()) {
                        createGeodatabase(fsinfo);
                    }
                }
            });


            return null;
        }
        protected Void onPostExecute(Long result){

            return null;
        }
    }
    EditText mSearchEditText;
    // The basemap switching menu items.
    MenuItem mStreetsMenuItem = null;
    MenuItem mTopoMenuItem = null;
    MenuItem mGrayMenuItem = null;
    MenuItem mOceansMenuItem = null;

    // Create MapOptions for each type of basemap.
    final MapOptions mTopoBasemap = new MapOptions(MapOptions.MapType.TOPO);
    final MapOptions mStreetsBasemap = new MapOptions(MapOptions.MapType.STREETS);
    final MapOptions mGrayBasemap = new MapOptions(MapOptions.MapType.GRAY);
    final MapOptions mOceansBasemap = new MapOptions(MapOptions.MapType.OCEANS);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.map);
        mMapView.enableWrapAround(true);
        mLocationLayer = new GraphicsLayer();
        mMapView.addLayer(mLocationLayer);
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            public void onStatusChanged(Object source, STATUS status) {
                if ((source == mMapView) && (status == OnStatusChangedListener.STATUS.INITIALIZED)) {
                    boolean mIsMapLoaded = true;
                }
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton zoomin = (FloatingActionButton) findViewById(R.id.zoomin);
        zoomin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, " Zooming in ", Toast.LENGTH_SHORT).show();

                mMapView.zoomin(true);
            }
        });
        FloatingActionButton zoomout = (FloatingActionButton) findViewById(R.id.zoomout);
        zoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, " Zooming out ", Toast.LENGTH_SHORT).show();
                mMapView.zoomout(true);
            }
        });
        FloatingActionButton mylocation = (FloatingActionButton) findViewById(R.id.mylocation);
        mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, " Loading GPS Coordinates ", Toast.LENGTH_SHORT).show();
                LocationDisplay = mMapView.getLocationDisplayManager();
                LocationDisplay.setAutoPanMode(AutoPanMode.LOCATION);
                LocationDisplay.start();
            }
        });


        FloatingActionButton offline = (FloatingActionButton) findViewById(R.id.offline);
        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, " Set the extent to download the map ", Toast.LENGTH_SHORT).show();
                try{
                    new ConnectToServer().execute().get(5,TimeUnit.SECONDS);

                }
                catch (Exception e) {
                    System.out.println("you are out");
                    e.printStackTrace();
                }

            }
        });
        FloatingActionButton measurment = (FloatingActionButton) findViewById(R.id.measurment);
        measurment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, " Click on map to add points and measure between them ", Toast.LENGTH_SHORT).show();
                SimpleMarkerSymbol measuremarkersymbol = new SimpleMarkerSymbol(Color.BLUE,5, SimpleMarkerSymbol.STYLE.CROSS);
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.YELLOW, 3);
                fillSymbol = new SimpleFillSymbol(Color.argb(100, 0, 225, 255));
                fillSymbol.setOutline(new SimpleLineSymbol(Color.TRANSPARENT, 0));

                // create the tool, required.
                MeasuringTool measuringTool = new MeasuringTool(mMapView);

                // customize the tool, optional.
                measuringTool.setLinearUnits(linearUnits);
                measuringTool.setMarkerSymbol(measuremarkersymbol);
                measuringTool.setLineSymbol(lineSymbol);
                measuringTool.setFillSymbol(fillSymbol);

                // fire up the tool, required.
                startActionMode(measuringTool);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        View searchRef = menu.findItem(R.id.action_search).getActionView();
        mSearchEditText = (EditText) searchRef.findViewById(R.id.searchText);

        mSearchEditText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    onSearchButtonClicked(mSearchEditText);
                    return true;
                }
                return false;
            }
        });
        // Get the basemap switching menu items.
        mStreetsMenuItem = menu.getItem(0);
        mTopoMenuItem = menu.getItem(1);
        mGrayMenuItem = menu.getItem(2);
        mOceansMenuItem = menu.getItem(3);

// Also set the topo basemap menu item to be checked, as this is the default.
        mTopoMenuItem.setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.World_Street_Map:
                mMapView.setMapOptions(mStreetsBasemap);
                mStreetsMenuItem.setChecked(true);
                return true;
            case R.id.World_Topo:
                mMapView.setMapOptions(mTopoBasemap);
                mTopoMenuItem.setChecked(true);
                return true;
            case R.id.Gray:
                mMapView.setMapOptions(mGrayBasemap);
                mGrayMenuItem.setChecked(true);
                return true;
            case R.id.Ocean_Basemap:
                mMapView.setMapOptions(mOceansBasemap);
                mOceansMenuItem.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private  void createGeodatabase(FeatureServiceInfo featureServerInfo) {
        // set up the parameters to generate a geodatabase
        GenerateGeodatabaseParameters GdbParams = new GenerateGeodatabaseParameters(featureServerInfo, mMapView.getExtent(),
                mMapView.getSpatialReference());
        GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {
            @Override
            public void statusUpdated(GeodatabaseStatusInfo geodatabaseStatusInfo) {
                Toast.makeText(MainActivity.this,"GDB created in Local Storage",Toast.LENGTH_SHORT).show();
            }
        };

        CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {
            @Override
            public void onError(final Throwable e) {
                progress.dismiss();
                Toast.makeText(MainActivity.this,"Error in creating GDB in Local Storage",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCallback(String gdbFileName) {
                progress.dismiss();
                // update map with local feature layer from geodatabase
                uploadLocalMap(gdbFileName);

            }
        };
        gdbSyncTask.generateGeodatabase(GdbParams, gdbFileName, false, statusCallback, gdbResponseCallback);
    }
    private void uploadLocalMap(String featureLayerPath) {
        // create a new geodatabase
        Geodatabase  localGdb=null;
        try {
            localGdb = new Geodatabase(featureLayerPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (localGdb != null) {
            for (GeodatabaseFeatureTable gdbFeatureTable : localGdb.getGeodatabaseTables()) {
                if (gdbFeatureTable.hasGeometry())
                    mMapView.addLayer(new FeatureLayer(gdbFeatureTable));
            }
        }
        // display the path to local geodatabase
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("GDB Update");
        dialog.setMessage(featureLayerPath);
    }





    public void onSearchButtonClicked(View view){
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        String address = mSearchEditText.getText().toString();
        executeLocatorTask(address);
    }
    private void executeLocatorTask(String address) {
        // Create Locator parameters from single line address string
        LocatorFindParameters findParams = new LocatorFindParameters(address);

        // Use the centre of the current map extent as the find location point
       // findParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
        //Log.d("Mapview is ",mMapView.getCenter().toString());
        // Calculate distance for find operation
        Envelope mapExtent = new Envelope();
        mMapView.getExtent().queryEnvelope(mapExtent);
        // assume map is in metres, other units wont work, double current envelope
        double distance = (mapExtent != null && mapExtent.getWidth() > 0) ? mapExtent.getWidth() * 2 : 10000;
        findParams.setDistance(distance);
        findParams.setMaxLocations(2);

        // Set address spatial reference to match map
        findParams.setOutSR(mMapView.getSpatialReference());
        Toast.makeText(MainActivity.this, "Async Task started", Toast.LENGTH_LONG).show();
        // Execute async task to find the address
        new LocatorAsyncTask().execute(findParams);

    }
    private class LocatorAsyncTask extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {
        private Exception mException;
        @Override
        protected List<LocatorGeocodeResult> doInBackground(LocatorFindParameters... params) {
            mException = null;
            List<LocatorGeocodeResult> results = null;
            Locator locator = Locator.createOnlineLocator();
            try {
                results = locator.find(params[0]);
            } catch (Exception e) {
                mException = e;
            }
            return results;
        }
        protected void onPostExecute(List<LocatorGeocodeResult> result) {
            if (mException != null) {
                Log.w("PlaceSearch", "LocatorSyncTask failed with:");
                mException.printStackTrace();
                Toast.makeText(MainActivity.this, getString(R.string.addressSearchFailed), Toast.LENGTH_LONG).show();
                return;
            }

            if (result.size() == 0) {
                Toast.makeText(MainActivity.this, getString(R.string.noResultsFound), Toast.LENGTH_LONG).show();
            } else {
                // Use first result in the list
                LocatorGeocodeResult geocodeResult = result.get(0);

                // get return geometry from geocode result
                Point resultPoint = geocodeResult.getLocation();
                // create marker symbol to represent location
                SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 16, SimpleMarkerSymbol.STYLE.CROSS);
                // create graphic object for resulting location
                Graphic resultLocGraphic = new Graphic(resultPoint, resultSymbol);
                // add graphic to location layer
                mLocationLayer.addGraphic(resultLocGraphic);

                // create text symbol for return address
                String address = geocodeResult.getAddress();
                TextSymbol resultAddress = new TextSymbol(20, address, Color.BLACK);
                // create offset for text
                resultAddress.setOffsetX(-4 * address.length());
                resultAddress.setOffsetY(10);
                // create a graphic object for address text
                Graphic resultText = new Graphic(resultPoint, resultAddress);
                // add address text graphic to location graphics layer
                mLocationLayer.addGraphic(resultText);



                // Zoom map to geocode result location
                mMapView.zoomToResolution(geocodeResult.getLocation(), 2);
            }
        }
    }

}
