package Controller;

import Model.Parcel;
import Model.User;
import org.jgrapht.Graph;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ServerCommunication {
    public Socket socket = null;
    public MainController mainController = null;
    private static ServerCommunication instance = null;

    public static ServerCommunication buildInstance(MainController mainController, Socket socket){
        if(instance == null){
            instance = new ServerCommunication(mainController, socket);
        }
        return instance;
    }

    public static ServerCommunication getInstance(){
        return instance;
    }

    private ServerCommunication(MainController mainController, Socket socket){
        this.mainController = mainController;
        this.socket = socket;
        initThreadPool();
        initResponsePool();
        startMessageListener();
    }

    private void initThreadPool(){
        threadPool = new ArrayList<>(22);
        for(int i=0;i<=22;i++){
            LinkedBlockingQueue<Thread> q = new LinkedBlockingQueue<>(3);
            threadPool.add(q);
        }
    }
    private void initResponsePool(){
        responsePool = new ArrayList<>(22);
        for(int i=0;i<=22;i++){
            LinkedBlockingQueue<CommunicationProtocol<Object>> q = new LinkedBlockingQueue<>(3);
            responsePool.add(q);
        }
    }

    //threadPool.get(i) represents a thread queue of the server request with the id i
    private List<LinkedBlockingQueue<Thread>> threadPool;

    //responsePool.get(i) represents the response queue of the server request with the id i
    private  List<LinkedBlockingQueue<CommunicationProtocol<Object>>> responsePool;




     //merge tot

     //TODO mai trebuie sa fac interfata pe mai multe limbi pentru postmanview si coordinatorview
     //TODO pe adminview merge pe engleza si romana
     //TODO pe italiana



    private void startMessageListener(){
        new Thread(() -> {
            while (true) {
                CommunicationProtocol<Object> response = Serializer.deserialize(socket);
                System.out.println("Got response " + Integer.toString(response.communicationPurpose));
                switch (response.communicationPurpose) {
                    case CommunicationProtocol.EXIT:
                        return;
                    case CommunicationProtocol.REQUEST_UPDATE_PARCELS:
                        requestUpdateParcels(response);
                        continue;
                    case CommunicationProtocol.REQUEST_UPDATE_COORDINATORS:
                        requestUpdateCoordinators(response);
                        continue;
                    case CommunicationProtocol.REQUEST_UPDATE_POSTMEN:
                        System.out.println("Notified for postmen update");
                        requestUpdatePostmen(response);
                        continue;
                    default:
                        break;
                }
                if (!threadPool.get(-response.communicationPurpose).isEmpty()) {
                    Thread t = null;
                    try {
                        t = threadPool.get(-response.communicationPurpose).take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responsePool.get(-response.communicationPurpose).offer(response);
                } else {
                    System.out.println("Hanging response without a sender");
                }
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void requestUpdateParcels(CommunicationProtocol<Object> request){
        Thread t = new Thread(() -> {
            mainController.updateParcels(((CommunicationProtocol<Parcel>)(Object) request).objects);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void requestUpdatePostmen(CommunicationProtocol<Object> request){
        Thread t = new Thread(() -> {
            mainController.updatePostmen(((CommunicationProtocol<User>)(Object) request).objects);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void requestUpdateCoordinators(CommunicationProtocol<Object> request){
        Thread t = new Thread(() -> {
            mainController.updateCoordinators(((CommunicationProtocol<User>)(Object) request).objects);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    public List<User> getUsers(){
        AtomicReference<CommunicationProtocol<User>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            CommunicationProtocol<Object> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_ALL_USERS, null);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<User>) (Object)
                        responsePool.get(CommunicationProtocol.GET_ALL_USERS).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_ALL_USERS).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects;
    }

    @SuppressWarnings("unchecked")
    public User verifyLogin(String username, String password){
        AtomicReference<CommunicationProtocol<User>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<String> list = new ArrayList<>(2);
            list.add(username);
            list.add(password);
            CommunicationProtocol<String> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.VERIFY_LOGIN, list);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<User>) (Object)
                        responsePool.get(CommunicationProtocol.VERIFY_LOGIN).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.VERIFY_LOGIN).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        User user = response.get().objects.get(0);
        if(user != null){
            mainController.switchView(user);
            return user;
        }
        return null;
    }

//    public User verifyLogin(String username, String password){
//        List<String> list = new ArrayList<>(2);
//        list.add(username);
//        list.add(password);
//        CommunicationProtocol<String> protocol = new CommunicationProtocol<String>(
//                CommunicationProtocol.VERIFY_LOGIN, list);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<User> response =  Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.VERIFY_LOGIN);
//        User user = response.objects.get(0);
//        if(user!=null) {
//            mainController.switchView(user);
//            return user;
//        }
//        return null;
//    }

    @SuppressWarnings("unchecked")
    public Graph getGraphLayout(){
        AtomicReference<CommunicationProtocol<Graph>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            CommunicationProtocol<Integer> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_GRAPH_LAYOUT, null);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Graph>) (Object)
                        responsePool.get(CommunicationProtocol.GET_GRAPH_LAYOUT).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_GRAPH_LAYOUT).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects.get(0);
    }

//    public Graph getGraphLayout(){
//        CommunicationProtocol<Object> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.GET_GRAPH_LAYOUT,null);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Graph> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.GET_GRAPH_LAYOUT);
//        return response.objects.get(0);
//    }
    @SuppressWarnings("unchecked")
    public Integer addUser(User user){
        AtomicReference<CommunicationProtocol<Integer>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<User> args = new ArrayList<>(1);
            args.add(user);
            CommunicationProtocol<User> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.INSERT_USER, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Integer>) (Object)
                        responsePool.get(CommunicationProtocol.INSERT_USER).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.INSERT_USER).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects.get(0);
    }

//    public Integer addUser(User user){
//        List<User> args = new ArrayList<>(1);
//        args.add(user);
//        CommunicationProtocol<User> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.INSERT_USER,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Integer> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.INSERT_USER);
//        return response.objects.get(0);
//    }

    @SuppressWarnings("unchecked")
    public void updateUser(User oldUser, User user){
        AtomicReference<CommunicationProtocol<Object>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<User> args = new ArrayList<>(2);
            args.add(oldUser);
            args.add(user);
            CommunicationProtocol<User> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.UPDATE_USER, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Object>) (Object)
                        responsePool.get(CommunicationProtocol.UPDATE_USER).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.UPDATE_USER).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void updateUser(User oldUser, User user){
//        List<User> args = new ArrayList<>(2);
//        args.add(oldUser);
//        args.add(user);
//        CommunicationProtocol<User> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.UPDATE_USER,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Object> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.UPDATE_USER);
//    }

    @SuppressWarnings("unchecked")
    public void deleteUser(User user){
        AtomicReference<CommunicationProtocol<Object>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<User> args = new ArrayList<>(1);
            args.add(user);
            CommunicationProtocol<User> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.DELETE_USER, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Object>) (Object)
                        responsePool.get(CommunicationProtocol.DELETE_USER).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.DELETE_USER).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void deleteUser(User user){
//        List<User> args = new ArrayList<>(1);
//        args.add(user);
//        CommunicationProtocol<User> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.DELETE_USER,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Object> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.DELETE_USER);
//    }

    public void exit(){
        Thread t = new Thread(() -> {
            CommunicationProtocol<Object> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.EXIT, null);
            Serializer.serialize(protocol, socket);
        });
        threadPool.get(CommunicationProtocol.EXIT).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void exit(){
//        CommunicationProtocol<Object> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.EXIT,null);
//        Serializer.serialize(protocol,socket);
//    }

    @SuppressWarnings("unchecked")
    public User getUserByUsername(String username){
        AtomicReference<CommunicationProtocol<User>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<String> args = new ArrayList<>(1);
            args.add(username);
            CommunicationProtocol<String> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_USER_BY_USERNAME, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<User>) (Object)
                        responsePool.get(CommunicationProtocol.GET_USER_BY_USERNAME).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_USER_BY_USERNAME).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects.get(0);
    }

//    public User getUserByUsername(String username){
//        List<String> args = new ArrayList<>(1);
//        args.add(username);
//        CommunicationProtocol<String> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.GET_USER_BY_USERNAME,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<User> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.GET_USER_BY_USERNAME);
//        return response.objects.get(0);
//    }

    @SuppressWarnings("unchecked")
    public User getUserByID(int id){
        AtomicReference<CommunicationProtocol<User>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<Integer> args = new ArrayList<>(1);
            args.add(id);
            CommunicationProtocol<Integer> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_USER_BY_ID, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<User>) (Object)
                        responsePool.get(CommunicationProtocol.GET_USER_BY_ID).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_USER_BY_ID).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects.get(0);
    }

//    public User getUserByID(int id){
//        List<Integer> args = new ArrayList<>(1);
//        args.add(id);
//        CommunicationProtocol<Integer> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.GET_USER_BY_ID,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<User> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.GET_USER_BY_USERNAME);
//        return response.objects.get(0);
//    }

    @SuppressWarnings("unchecked")
    public List<Parcel> getParcels(){
        AtomicReference<CommunicationProtocol<Parcel>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            CommunicationProtocol<Object> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_ALL_PARCELS, null);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Parcel>) (Object)
                        responsePool.get(CommunicationProtocol.GET_ALL_PARCELS).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_ALL_PARCELS).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects;
    }

    @SuppressWarnings("unchecked")
    public List<Parcel> getParcelsByPostmanID(int ID){
        AtomicReference<CommunicationProtocol<Parcel>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<Integer> args = new ArrayList<>(1);
            args.add(ID);
            CommunicationProtocol<Integer> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Parcel>) (Object)
                        responsePool.get(CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects;
    }


//    public List<Parcel> getParcelsByPostmanID(int ID){
//        List<Integer> args = new ArrayList<>(1);
//        args.add(ID);
//        CommunicationProtocol<Integer> protocol = new CommunicationProtocol<Integer>(
//                CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Parcel> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID);
//        return response.objects;
//    }

    @SuppressWarnings("unchecked")
    public Parcel getParcelByID(int ID){
        AtomicReference<CommunicationProtocol<Parcel>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<Integer> args = new ArrayList<>(1);
            args.add(ID);
            CommunicationProtocol<Integer> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.GET_PARCELS_BY_ID, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Parcel>) (Object)
                        responsePool.get(CommunicationProtocol.GET_PARCELS_BY_ID).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.GET_PARCELS_BY_ID).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects.get(0);
    }

//    public Parcel getParcelByID(int ID){
//        List<Integer> args = new ArrayList<>(1);
//        args.add(ID);
//        CommunicationProtocol<Integer> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.GET_PARCELS_BY_ID,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Parcel> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.GET_PARCELS_BY_ID);
//        return response.objects.get(0);
//    }

    @SuppressWarnings("unchecked")
    public int insertParcel(Parcel parcel){
        AtomicReference<CommunicationProtocol<Integer>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<Parcel> args = new ArrayList<>(1);
            args.add(parcel);
            CommunicationProtocol<Parcel> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.INSERT_PARCEL, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Integer>) (Object)
                        responsePool.get(CommunicationProtocol.INSERT_PARCEL).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.INSERT_PARCEL).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.get().objects.get(0);
    }

//    public int insertParcel(Parcel parcel){
//        List<Parcel> args = new ArrayList<>(1);
//        args.add(parcel);
//        CommunicationProtocol<Parcel> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.INSERT_PARCEL,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Integer> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.INSERT_PARCEL);
//        return response.objects.get(0);
//    }

    @SuppressWarnings("unchecked")
    public void updateParcel(Parcel parcel){
        AtomicReference<CommunicationProtocol<Object>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<Parcel> args = new ArrayList<>(1);
            args.add(parcel);
            CommunicationProtocol<Parcel> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.UPDATE_PARCEL, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Object>) (Object)
                        responsePool.get(CommunicationProtocol.UPDATE_PARCEL).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.UPDATE_PARCEL).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void updateParcel(Parcel parcel){
//        List<Parcel> args = new ArrayList<>(1);
//        args.add(parcel);
//        CommunicationProtocol<Parcel> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.UPDATE_PARCEL,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Object> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.UPDATE_PARCEL);
//    }

    @SuppressWarnings("unchecked")
    public void deleteParcel(Parcel parcel){
        AtomicReference<CommunicationProtocol<Object>> response = new AtomicReference<>();
        Thread t = new Thread(() -> {
            List<Parcel> args = new ArrayList<>(1);
            args.add(parcel);
            CommunicationProtocol<Parcel> protocol = new CommunicationProtocol<>(
                    CommunicationProtocol.DELETE_PARCEL, args);
            Serializer.serialize(protocol, socket);
            try {
                response.set((CommunicationProtocol<Object>) (Object)
                        responsePool.get(CommunicationProtocol.DELETE_PARCEL).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadPool.get(CommunicationProtocol.DELETE_PARCEL).offer(t);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void deleteParcel(Parcel parcel){
//        List<Parcel> args = new ArrayList<>(1);
//        args.add(parcel);
//        CommunicationProtocol<Parcel> protocol = new CommunicationProtocol<>(
//                CommunicationProtocol.DELETE_PARCEL,args);
//        Serializer.serialize(protocol,socket);
//        CommunicationProtocol<Object> response = Serializer.deserialize(socket);
//        assert(response.communicationPurpose == -CommunicationProtocol.DELETE_PARCEL);
//    }

}
