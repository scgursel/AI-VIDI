package com.example.testdb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    int i=0;
    ArrayList<SaveText> saveTextArrayList;
    Context context;
    private TextToSpeech narrator;

    public Adapter(ArrayList<SaveText> saveTextArrayList, Context context) {
        this.saveTextArrayList = saveTextArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.entity_savetext,parent,false);
        MyViewHolder myViewHolder=new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.text.setText(saveTextArrayList.get(position).getText()+"");
        holder.file_name.setText(saveTextArrayList.get(position).getFile_name()+"");

        holder.img_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_dialog(position);

            }
        });

        holder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                update_dialog(position);
                return false;
            }
        });

        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i++;
                Handler handler= new Handler();
                Runnable run=new Runnable() {
                    @Override
                    public void run() {
                        i=0;
                    }
                };
                if(i==2){
                    tts_text(position);
                }

                if(i==1){
                    handler.postDelayed(run,500);
                    tts_baslik(position);
                }
            }
        });
        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_dialog(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return saveTextArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        TextView file_name;

        ImageView img_update;
        ImageView img_delete;
        ImageView img_tts;

        public MyViewHolder(View itemview) {

            super(itemview);
            this.file_name = (TextView) itemview.findViewById(R.id.file_name);
            //   file_image.setBackgroundResource(R.drawable.vidi);
            this.text = (TextView) itemview.findViewById(R.id.text);
            this.img_delete = (ImageView) itemview.findViewById(R.id.img_delete);
            this.img_update = (ImageView) itemview.findViewById(R.id.img_update);
            this.img_tts=(ImageView) itemview.findViewById(R.id.img_tts);

        }

    }


    //===========UPDATE===============
    public void update_dialog(int position_of_update){


        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Update");

        LayoutInflater li=LayoutInflater.from(context);
        View view_update=li.inflate(R.layout.update_savetext,null);
        builder.setView(view_update);

        EditText edt_filename=(EditText)view_update.findViewById(R.id.edt_filename);


        edt_filename.setText(saveTextArrayList.get(position_of_update).getFile_name()+"");


        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                SaveTextRepository saveTextRepository=new SaveTextRepository(context);
                String updated_filename=edt_filename.getText().toString().trim();
                String updated_text=saveTextArrayList.get(position_of_update).getText()+"";

                SaveText saveText_updated=new SaveText(saveTextArrayList.get(position_of_update).getId(),updated_text,updated_filename);
                saveTextRepository.UpdateTask(saveText_updated);

                saveTextArrayList.get(position_of_update).setText(updated_text);
                saveTextArrayList.get(position_of_update).setFile_name(updated_filename);

                notifyDataSetChanged();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    //===========UPDATE FİNAL===============

    //===========DELETE START===============

    public void delete_dialog(int position_of_delete){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Warning");
        builder.setMessage("Emin misiniz?");

        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SaveTextRepository saveTextRepository=new SaveTextRepository(context);
                saveTextRepository.DeleteTask(saveTextArrayList.get(position_of_delete));


                saveTextArrayList.remove(position_of_delete);
                notifyDataSetChanged();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("İptal et", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //==============DELETE FİNAL==================

    //==============TTS START=====================
    public void tts_text(int position_of_tts){
        String tts=saveTextArrayList.get(position_of_tts).getText()+"";
        String fName=saveTextArrayList.get(position_of_tts).getFile_name()+"";

        narrator=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) {
                    Locale locale = new Locale("tr", "TR");
                    narrator.setLanguage(locale);
                    narrator.speak(" Belge İçeriği"+tts, TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });
    }

    public void tts_baslik(int position_of_tts){
        String tts=saveTextArrayList.get(position_of_tts).getText()+"";
        String fName=saveTextArrayList.get(position_of_tts).getFile_name()+"";

        narrator=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) {
                    Locale locale = new Locale("tr", "TR");
                    narrator.setLanguage(locale);
                    narrator.speak("Belge Başlığı"+fName, TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });
    }


    //===========TTS FİNAL============





}