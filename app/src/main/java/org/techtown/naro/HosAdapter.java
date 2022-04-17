package org.techtown.naro;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class HosAdapter extends RecyclerView.Adapter<HosAdapter.ViewHolder> {

    Hospital hospital;
    HospitalDB helper;
    Double lat;
    Double lon;
    String name;
    String address;
    String tel;
    String onoff;
    GoogleMap googleMap;
    ArrayList<Hospital> info = new ArrayList<>();

    @NonNull
    @Override
    public HosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View hosView = inflater.inflate(R.layout.hos_item, parent, false);
        return new ViewHolder(hosView);
    }

    @Override
    public void onBindViewHolder(@NonNull HosAdapter.ViewHolder holder, int position) {
        hospital = info.get(holder.getAdapterPosition());
        holder.setInfo(hospital);

        //동물병원 클릭하면 동물병원에 대한 정보로 넘어가는 이벤트
        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                Context context = view.getContext();
                helper = new HospitalDB(context.getApplicationContext());
                lat = info.get(pos).getHoslatitude();
                lon = info.get(pos).getHoslongitude();
                name = info.get(pos).getHosname();
                address = info.get(pos).getHosaddress();
                tel = info.get(pos).getTel();
                onoff = info.get(pos).getHosonoff();

                //아이템 누르면 다음 액티비티로 넘어가는 데이터들
                Intent intent = new Intent(context.getApplicationContext(),HosMap.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("hospitalname",name);
                intent.putExtra("hospitaladdress",address);
                intent.putExtra("latitude",lat);
                intent.putExtra("longitude",lon);
                intent.putExtra("tel",tel);
                context.getApplicationContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return info.size();
    }
    public void addItem(Hospital hos){
        info.add(hos);
    }
    public void removeAllItem(){
        info.clear();
    }



    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView hosname;
        private TextView address;
        private TextView onoff;
        public ViewHolder(View hosView){
            super(hosView);
            hosname = hosView.findViewById(R.id.hosname);
            address = hosView.findViewById(R.id.hosaddress);
            onoff = hosView.findViewById(R.id.onoff);

        }
        public void setInfo(Hospital hos){
            hosname.setText(hos.getHosname());
            address.setText(hos.getHosaddress());
            onoff.setText(hos.getHosonoff());
        }
    }
}
