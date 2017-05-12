package bq.yournote.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import bq.yournote.Views.CanvasView;
import bq.yournote.R;

public class PaintActivity extends AppCompatActivity {
    private CanvasView canvasView;
    private Bitmap mBitmap;
    private ImageView imageView;
    private TextView txtResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        canvasView = (CanvasView) findViewById(R.id.canvas);
        imageView = (ImageView) findViewById(R.id.imageView);
        txtResult = (TextView) findViewById(R.id.resultado);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imageView.setImageBitmap(canvasView.getmBitmap());
                Bitmap bitmap = canvasView.getmBitmap();

                //Con el siguiente codigo podemos pasar de bitmap a texto
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!textRecognizer.isOperational())
                    Log.e("ERROR","Las depencias no estan disponibles");
                else{
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i=0;i<items.size();++i)
                    {
                        TextBlock item = items.valueAt(i);
                        stringBuilder.append(item.getValue());
                        stringBuilder.append("\n");
                    }
                    System.out.println("Resultado: "+stringBuilder.toString());
                    txtResult.setText(stringBuilder.toString());
                }

            }
        });


    }

    public void clearCanvas(){
        canvasView.clearCanvas();
    }

}
