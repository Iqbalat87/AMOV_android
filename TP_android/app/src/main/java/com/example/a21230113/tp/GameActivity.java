package com.example.a21230113.tp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;


public class GameActivity extends AppCompatActivity{

    private int nSeconds = 1000;
    private boolean []mapBool;
    private boolean flag = false;
    private boolean canClick = true;
    private boolean isDoubleClick = false;
    private boolean player1ToPlay = true;
    private boolean levelEdited;
    private Integer card = -1;
    private int doubleClick = 0;
    private ImageView imgView;
    private ImageView img;
    private Level level;
    private GridView gv;
    private TextView tvCred;
    private TextView tvSco;
    private int score1 = 0;
    private int score = 0;
    private String typeGame;

    private static final int PORT = 8899;
    ProgressDialog pd = null;
    ServerSocket serverSocket=null;
    Socket socketGame = null;
    BufferedReader input;
    PrintWriter output;
    Handler procMsg = null;
    ArrayList<Level> levels = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        levels = new ArrayList<>();
        levels.add(new Level(4, getString(R.string.beginner),null));
        levels.add(new Level(8, getString(R.string.easy),null));
        levels.add(new Level(10, getString(R.string.medium),null));
        levels.add(new Level(20, getString(R.string.hard),null));
        levels.add(new Level(30, getString(R.string.professional), null));

        readFromFileLevels();

        procMsg = new Handler();

        Intent intent = getIntent();

        String levelReceived = intent.getStringExtra("level");
        for(int i = 0; i < levels.size(); i++){
            if(levelReceived.equalsIgnoreCase(levels.get(i).getNameLevel())){
                this.level = levels.get(i);
                break;
            }
        }

        typeGame = intent.getStringExtra("type");
        mapBool = new boolean[level.getNCards()];
        for(int i=0; i<level.getNCards(); i++) mapBool[i] = false;

        tvCred = (TextView) findViewById(R.id.textView5);
        tvSco = (TextView) findViewById(R.id.textView6);
        if(typeGame.equalsIgnoreCase("multi1")) {
            tvCred.setText(getString(R.string.player) + "1: " + score1);
            tvSco.setText(getString(R.string.player) + "2: " + score);
        }else {
            if (typeGame.contains("multi2")) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
                    Toast.makeText(this, getString(R.string.no_net_con), Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                if(typeGame.equalsIgnoreCase("multi2cli"))
                    canClick = false;
            } else {
                int credits = 3;
                tvCred.setText(getString(R.string.credits) + ": " + credits);
                tvSco.setText(getString(R.string.score) + ": " + score);
            }
        }


        if(level.getNameLevel().equals(getString(R.string.beginner)) || level.getNameLevel().equals(getString(R.string.easy)) ||
                level.getNameLevel().equals(getString(R.string.medium)) || level.getNameLevel().equals(getString(R.string.hard)) ||
                level.getNameLevel().equals(getString(R.string.professional)))
            levelEdited = false;
        else
            levelEdited = true;


        gv = (GridView) findViewById(R.id.grid);
        gv.setAdapter(new ImageAdapter(this, level, levelEdited));
        gv.setLongClickable(false); gv.setOnLongClickListener(null);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                performCertainAction(view, position);
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (typeGame.equalsIgnoreCase("multi2serv"))
            server();
        else if(typeGame.equalsIgnoreCase("multi2cli"))// CLIENT
            clientDlg();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, item.toString() + " " + getString(R.string.chosen) + "!", Toast.LENGTH_SHORT).show();
        if(item.toString().equalsIgnoreCase(getString(R.string.touch_simple))){
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    performCertainAction(view, position);
                }
            });
            gv.setOnItemLongClickListener(null);
            isDoubleClick = false;
        }
        if(item.toString().equalsIgnoreCase(getString(R.string.touch_long))){
            isDoubleClick = false;
            gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    performCertainAction(view, position);
                    return true;
                }
            });
            gv.setOnItemClickListener(null);
        }
        if(item.toString().equalsIgnoreCase(getString(R.string.touch_double))){
            gv.setOnItemLongClickListener(null);
            isDoubleClick = true;
            final EditText edtIP = new EditText(this);
            edtIP.setText("1");
            AlertDialog ad = new AlertDialog.Builder(this).setTitle(getString(R.string.touch_double))
                    .setMessage(getString(R.string.seconds)).setView(edtIP)
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            nSeconds = Integer.parseInt(edtIP.getText().toString()) * 1000;
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.infinite), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            nSeconds = Integer.MAX_VALUE;
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    }).create();
            ad.show();
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    performCertainAction(view, position);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void readFromFileLevels() {
        try {
            FileInputStream fis = openFileInput("fileLevels3.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);


            List<Level> level;
            level = (List<Level>) ois.readObject();
            ois.close();
            levels.addAll(level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void moveMyPlayer(int move) {
        if (move == 2) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        output.println(2);
                        output.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    private void showADialog(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        if(typeGame.equalsIgnoreCase("multi1")){
            if(score1 > score) builder.setTitle(getString(R.string.player) + "1 " + getString(R.string.won) + "!");
            else{ if( score > 0) builder.setTitle(getString(R.string.player) + "2 " + getString(R.string.won) + "!");
            else builder.setTitle(getString(R.string.tied_game));
            }
        }
        else builder.setTitle(R.string.game_finished);
        builder.setMessage(R.string.choose_option)
                .setPositiveButton(getString(R.string.historic), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialogB = (Dialog) dialog;
                        Intent intent = new Intent(dialogB.getContext(), HistoricActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.new_game), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialogB = (Dialog) dialog;
                        Intent intent = new Intent(dialogB.getContext(), SettingsActivity.class);
                        startActivity(intent);
                    }
                });
        builder.show();
        writeToFile();
    }

    public void performCertainAction(View view, int position){
        if(isDoubleClick) {
            doubleClick++;
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    doubleClick = 0;
                }
            };
            if (doubleClick == 1) {
                handler.postDelayed(r, nSeconds);
                return;
            } else if (doubleClick == 2) doubleClick = 0;
        }
        if(mapBool[position] || !canClick) return;
        img = (ImageView) view;
        if(!levelEdited)
            img.setImageResource(ImageAdapter.imagesList.get(position));
        else
            img.setImageBitmap(ImageAdapter.imagesEdited.get(position));
        if (flag) {
            canClick = false;
            boolean condition;
            if(!levelEdited)
                condition = !ImageAdapter.imagesList.get(position).equals(ImageAdapter.imagesList.get(card));
            else
                condition = !ImageAdapter.imagesEdited.get(position).equals(ImageAdapter.imagesEdited.get(card));
            if (condition) {
                mapBool[position] = mapBool[card] = false;
                if(typeGame.equalsIgnoreCase("multi1")) {
                    if (player1ToPlay)
                        Toast.makeText(GameActivity.this, getString(R.string.player) + " 2 " + getString(R.string.turn)+"!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(GameActivity.this, getString(R.string.player) + " 1 " + getString(R.string.turn)+"!", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(GameActivity.this, R.string.cards_dif, Toast.LENGTH_SHORT).show();
                player1ToPlay = !player1ToPlay;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageResource(R.drawable.back);
                                imgView.setImageResource(R.drawable.back);
                                canClick = true;
                            }
                        });
                    }
                }).start();
                flag = false;
            } else {
                if(typeGame.equalsIgnoreCase("multi1")) {
                    if (player1ToPlay)
                        Toast.makeText(GameActivity.this, getString(R.string.play_again) + " 1!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(GameActivity.this, getString(R.string.play_again) + "2!", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(GameActivity.this, R.string.cards_equ, Toast.LENGTH_SHORT).show();
                flag = false;
                mapBool[position] = true;
                mapBool[card] = true;
                canClick = true;

                if(typeGame.equalsIgnoreCase("multi1")) {
                    if (player1ToPlay){ score1+=5; tvCred.setText(getString(R.string.player)+"1: " + score1); }
                    else {score += 5; tvSco.setText(getString(R.string.player)+"2: " + score); }
                }
                else{ score+=5; tvSco.setText(getString(R.string.score) + ": " + score);}

                moveMyPlayer(2);
            }
        } else {
            mapBool[position] = true;
            flag = true;
            card = position;
            imgView = img;
        }
        int i;
        for (i = 0; i < level.getNCards(); i++)
            if (!mapBool[i]) break;
        if (i == level.getNCards())
            showADialog(GameActivity.this);
    }

    private void writeToFile() {
        readFromFile();
        BufferedWriter bw = null;
        try{
            FileOutputStream fos = openFileOutput("histFile", Context.MODE_PRIVATE | MODE_APPEND);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(level.getNameLevel() + ": " + tvCred.getText() + " -> "+  tvSco.getText() + '\n');
            bw.newLine();

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                bw.close();
            }catch (Exception e){ e.printStackTrace(); }
        }
    }

    public void readFromFile() {
        Scanner fileScanner = new Scanner("histFile");
        fileScanner.nextLine();
        try{
            FileWriter fileStream = new FileWriter("histFile");
            BufferedWriter out = new BufferedWriter(fileStream);
            while(fileScanner.hasNextLine()) {
                String next = fileScanner.nextLine();
                if(next.equals("\n")) out.newLine();
                else out.write(next);
                out.newLine();
            }
            out.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    void server() {
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.serverdlg_msg) + "\n(IP: " + ip + ")");
        pd.setTitle(R.string.serverdlg_title);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
                if (serverSocket!=null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    serverSocket=null;
                }
            }
        });
        pd.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket=null;
                    commThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    socketGame = null;
                }
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        if (socketGame == null)
                            finish();
                    }
                });
            }
        });
        t.start();
    }

    void clientDlg() {
        final EditText edtIP = new EditText(this);
        edtIP.setText("192.168.1.102");
        AlertDialog ad = new AlertDialog.Builder(this).setTitle(getString(R.string.game_client))
                .setMessage(getString(R.string.server_ip)).setView(edtIP)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client(edtIP.getText().toString(), PORT); // to test with emulators: PORTaux);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).create();
        ad.show();
    }

    void client(final String strIP, final int Port) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketGame = new Socket(strIP, Port);
                } catch (Exception e) {
                    socketGame = null;
                }
                if (socketGame == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    return;
                }
                commThread.start();
            }
        });
        t.start();
    }

    Thread commThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socketGame.getInputStream()));
                output = new PrintWriter(socketGame.getOutputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    final int itnmove = Integer.parseInt(read);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Recebido " + itnmove, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Toast.makeText(getApplicationContext(),getString(R.string.game_finished), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}


class ImageAdapter extends BaseAdapter{
    static List<Integer> imagesList;
    static List<Bitmap> imagesEdited;
    private Context context;
    private Level level;
    private int nCards;
    private boolean edited;

    public ImageAdapter(Context c, Level level, boolean edited){
        context = c;
        this.level = level;
        this.nCards = level.getNCards();
        this.edited = edited;
        initialize();
    }

    public void initialize(){

        if(edited) {
            List<Bitmap> bm;
            imagesEdited = new ArrayList<>();
            bm = level.getBitmapArray();
            imagesEdited = new ArrayList<>();
            Boolean [] colocado = new Boolean[bm.size()*2];
            for(int j = 0 ; j < bm.size()*2; j++){
                colocado[j] = false;
                imagesEdited.add(null);
            }
            int contador;
            for(int c = 0 ; c < bm.size(); c++ ){
                contador = 0;
                for(int i = 0; i < 2; i++) {
                    while (contador < 2) {
                        int n = new java.util.Random().nextInt(bm.size() * 2);
                        if (!colocado[n]) {
                            imagesEdited.set(n, bm.get(c));
                            colocado[n] = true;
                            contador += 1;
                        }
                    }
                }
            }
        }
        else {
            TypedArray imgs = context.getResources().obtainTypedArray(R.array.loading_images);

            List<Integer> randoms = new ArrayList<>();
            for (int i = 0; i < nCards / 2; ) {
                int r = (int) (Math.random() * imgs.length());
                if (!randoms.contains(r)) {
                    randoms.add(r);
                    ++i;
                }
            }
            List<Integer> clonedList = new ArrayList<>();
            clonedList.addAll(randoms);
            clonedList.addAll(randoms);
            Collections.shuffle(clonedList);
            imagesList = new ArrayList<>();
            for (int i = 0; i < nCards; i++) {
                imagesList.add(null);
                imagesList.set(i, imgs.getResourceId(clonedList.get(i), R.drawable.france));
            }
        }
    }

    public int getCount(){
        if(edited)
            return imagesEdited.size();
        else
            return imagesList.size();
    }

    public Object getItem(int position){
        return null;
    }

    public long getItemId(int position){
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ImageView imageView;
        if (convertView == null) {
            Resources r = Resources.getSystem();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());


            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams((int) px, (int) px));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(Color.BLUE);

        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.back);
        return imageView;
    }

}
