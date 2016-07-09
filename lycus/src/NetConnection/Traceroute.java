//package NetConnection;
//
//import java.io.IOException;
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.UnknownHostException;
//import java.util.Arrays;
//
////import jpcap.JpcapCaptor;
////import jpcap.JpcapSender;
////import jpcap.NetworkInterface;
////import net.sourceforge.jpcap.client.NetworkInterfaceAddress;
//import net.sourceforge.jpcap.net.EthernetPacket;
//import net.sourceforge.jpcap.net.ICMPPacket;
//import net.sourceforge.jpcap.net.IPPacket;
//import net.sourceforge.jpcap.net.Packet;
//import net.sourceforge.jpcap.net.TCPPacket;
//
///**
// *  Encapsulates ICMP interface that can be used to ping or trace route remote
// *  hosts. 
// *  
// *  @author Mikica B Kocic
// *
// */
//public class Traceroute implements Runnable
//{
//    /**
//     *  Provides message presentation context to instance of <code>Traceroute</code>.
//     */
//    public interface Context
//    {
//        /**
//         *  Logs new line
//         */
//        public abstract void logNewLine ();
//        
//        /**
//         *  Logs message, with optional timestamp
//         *  
//         *  @param str        message that will be logged
//         *  @param timestamp  indicates whether to put timestamp in front
//         */
//        public abstract void logMessage( String str, boolean timestamp );
//        
//        /**
//         *  Changed status
//         */
//        public abstract void onTracerouteCompleted ();
//    }
//
//    //////////////////////////////////////////////////////////////////////////////////////
//
//    /**
//     *  Instance of the thread that sends ICMP packets
//     */
//    private Thread tracingThread;
//    
//    /**
//     *  Indicates if thread is (or should be) running
//     */
//    private volatile boolean running = false;
//    
//    /**
//     *  Indicates that thread has been completed
//     */
//    private volatile boolean completed;
//    
//    /**
//     *  Jpcap <code>NetworkInterface</code> device count. 
//     */
//    private int deviceCount = 0;
//    
//    /**
//     *  Instance of the Jpcap capturing class
//     */
//    private JpcapCaptor captor = null;
//    
//    /**
//     *  Instance of the Jpcap network interface used for sending and 
//     *  receiving ICMP packets
//     */
//    private NetworkInterface device = null;
//    
//    /**
//     *  Local IP address
//     */
//    private InetAddress localIP = null;
//    
//    /**
//     *  Indicates whether to resolve addresses to names or not. By default
//     *  disabled, because resolving will slow down trace route presentation.
//     */
//    private boolean resolveNames = false;
//    
//    /**
//     *  Host name or IP address to ping
//     */
//    private String hostName;
//    
//    /**
//     *  Initial TTL (time to live or hop count). When set to 0, thread will do
//     *  trace route. When set to e.g. 64, thread will do ping.
//     */
//    private int initialTTL;
//    
//    /**
//     *  Presentation context for messages
//     */
//    private Context context;
//
//    //////////////////////////////////////////////////////////////////////////////////////
//
//    /**
//     *  Creates new instance of <code>Traceroute</code>
//     *  
//     *  @param context   where to log messages
//     */
//    public Traceroute( Context context )
//    {
//        this.context       = context;
//        this.running       = false;
//        this.completed     = true;
//        this.tracingThread = null;
//        
//        deviceCount = JpcapCaptor.getDeviceList ().length;
//    }
//
//    /**
//     *  Prints line to log area prefixed with timestamp 
//     */
//    private void println( String str )
//    {
//        context.logMessage( str, /*timestamp*/ true );
//        context.logNewLine ();
//    }
//
//    /**
//     *  Advances to new line in log area 
//     */
//    private void println ()
//    {
//        context.logNewLine ();
//    }
//    
//    /**
//     *  Prints characters to log area optionally prefixed with timestamp
//     *  
//     *  @param str         message to log
//     *  @param timestamp   whether to prefix message with timestamp or not
//     */
//    private void print( String str, boolean timestamp )
//    {
//        context.logMessage( str,timestamp );
//    }
//    
//    /**
//     *  Starts thread that will trace route to given host. The instance is 
//     *  locked in the mean time, so other trace routes could not start in parallel.
//     *  To start trace-route <code>initialTTL</code> must be set to 0. To start
//     *  ping instead, set <code>initialTTL</code> to 64.
//     *  
//     *  @param deviceNo    network interface on which to start pinging
//     *  @param hostName    target host address or host name
//     *  @param initialTTL  initial hop limit (or time-to-live)
//     */
//    public void startPinging( int deviceNo, String hostName, int initialTTL )
//    {
//        synchronized( this )
//        {
//            if ( ! completed ) {  // Allows only one thread per instance
//                return;
//            }
//    
//            /* Set thread parameters
//             */
//            openDeviceOnInterface( deviceNo );
//            this.hostName   = hostName;
//            this.initialTTL = initialTTL;
//
//            /* Enable thread
//             */
//            running   = true;
//            completed = false;
//
//            /* Start thread
//             */
//            tracingThread = new Thread( this );
//            tracingThread.start ();
//        }
//    }
//
//    /**
//     *  Stops on-going trace route or ping
//     */
//    public void stopTrace ()
//    {
//        synchronized( this )
//        {
//            running = false; // signal thread to exit
//            this.notifyAll (); // interrupt any sleep
//        }
//
//        print( " <interrupt> ", /*timestamp*/ false );
//    }
//
//    /**
//     *  Returns if IDLE, i.e. if tracing thread does not exist or previous thread has
//     *  been completed.
//     */
//    public boolean isIdle ()
//    {
//        return completed;
//    }
//
//    /**
//     *  Dumps details about particular Jpcap network interface into log area
//     *  
//     *  @param title  title line
//     *  @param ni     network interface to show 
//     */
//    public void dumpInterfaceInfo( String title, NetworkInterface ni )
//    {
//        println( title );
//        println( "    Desc: " + ni.description );
//        println( "    Name: " + ni.name );
//        for( NetworkInterfaceAddress na : ni.addresses )
//        {
//            println( "    Addr: " + na.address );
//        }
//    }
//    
//    /**
//     *  Gets array of interface descriptions (suitable for the JComboBox)
//     *  
//     *  @return array of strings with descriptions
//     */
//    public String[] getInterfaceList ()
//    {
//        this.deviceCount = JpcapCaptor.getDeviceList ().length;
//        String[] devList = new String[ this.deviceCount ];
//        
//        int ni_index = 0;
//        for( NetworkInterface ni : JpcapCaptor.getDeviceList () )
//        {
//            String ourDescription = ni.description;
//            for( NetworkInterfaceAddress addr : ni.addresses )
//            {
//                if( addr.address instanceof Inet4Address ) {
//                    ourDescription = addr.address.toString () + " -- " + ni.description;
//                    break;
//                }
//            }
//
//            devList[ ni_index ] = " iface" + ni_index + " -- " + ourDescription;
//
//            dumpInterfaceInfo( "Interface " + (ni_index++), ni );
//        }
//        
//        return devList;
//    }
//    
//    /*
//     *  Open Jpcap device to send/receive on particular network interface
//     *  
//     *  @param deviceNo  device index (e.g., 0, 1..)
//     */
//    private void openDeviceOnInterface( int deviceNo )
//    {
//        // Open specified device from the list
//        //
//        device = JpcapCaptor.getDeviceList()[ deviceNo ];
//        localIP = null;
//        captor = null;
//        
//        try
//        {
//            captor = JpcapCaptor.openDevice( device, 
//                    /*MTU*/ 2000, /*promiscuous*/ false, /*timeout*/ 1 );
//            
//            // captor.setNonBlockingMode( true );
//            // captor.setPacketReadTimeout( 1000 );
//
//            for( NetworkInterfaceAddress addr : device.addresses )
//            {
//                if( addr.address instanceof Inet4Address ) {
//                    localIP = addr.address;
//                    break;
//                }
//            }
//        }
//        catch ( IOException e )
//        {
//            device  = null;
//            localIP = null;
//            captor  = null;
//        }
//    }
//
//    /**
//     *  Interruptible sleep (replacement for <code>Thread.sleep</code>).
//     *  
//     *  @param millis - the length of time to sleep in milliseconds. 
//     */
//    private void interruptibleSleep( int millis )
//    {
//        synchronized( this )
//        {
//            try {
//                this.wait( millis );
//            }
//            catch( InterruptedException ie ) {
//                running = false; // kills the thread
//            }
//        }
//    }
//    
//    /**
//     *  Obtains MAC address of the default gateway for captor interface.
//     *  
//     *  @return MAC address as byte array
//     */
//    private byte[] obtainDefaultGatewayMac( String httpHostToCheck )
//    {
//        print( "Obtaining default gateway MAC address... ", true );
//        
//        byte[] gatewayMAC = null;
//        
//        if ( captor != null ) try
//        {
//            InetAddress hostAddr = InetAddress.getByName( httpHostToCheck );
//            captor.setFilter( "tcp and dst host " + hostAddr.getHostAddress(), true );
//            
//            int timeoutTimer = 0;
//            new URL("http://" + httpHostToCheck ).openStream().close();
//            
//            while( running )
//            {
//                Packet ping = captor.getPacket ();
//                
//                if( ping == null )
//                {
//                    if ( timeoutTimer < 20 ) { // max 2 sec
//                        interruptibleSleep( 100  /*millis*/ );
//                        ++timeoutTimer;
//                        continue;
//                    }
//                    /* else: Timeout exceeded
//                     */
//                    print( "<timeout>", /*timestamp*/ false );
//                    println( "ERROR: Cannot obtain MAC address for default gateway." );
//                    println( "Maybe there is no default gateway on selected interface?" );
//                    return gatewayMAC;
//                }
//                
//                byte[] destinationMAC = ((EthernetPacket)ping.datalink).dst_mac; 
//                
//                if( ! Arrays.equals( destinationMAC, device.mac_address ) ) {
//                    gatewayMAC = destinationMAC;
//                    break;
//                }
//
//                timeoutTimer = 0; // restart timer
//                new URL("http://" + httpHostToCheck ).openStream().close();
//            }
//        }
//        catch( MalformedURLException e )
//        {
//            println( "Invalid URL: " + e.toString () );
//        }
//        catch( UnknownHostException e )
//        {
//            println( "Unknown host: " + httpHostToCheck );
//        }
//        catch( IOException e )
//        {
//            println( "ERROR: " + e.toString () );
//        }
//        
//        print( " OK.", /*timestamp*/ false );
//        println ();
//        return gatewayMAC;
//    }
//
//    /**
//     *  Scan TCP ports 
//     *
//     *  @throws UnknownHostException 
//     */
//    public boolean tcpPortScan ( int localPort, String remoteHost, int rp1, int rp2 )
//        throws UnknownHostException, IOException
//    {
//        println( "-------------------------------------------------" );
//        print( "Looking up " + hostName + "...", /*timestamp*/ true );
//        
//        InetAddress remoteIP = InetAddress.getByName( remoteHost );
//        
//        print( "  " + remoteIP.getHostAddress (), /*timestamp*/ false );
//        println ();
//
//        byte[] defaultGatewayMAC = obtainDefaultGatewayMac( "dsv.su.se" );
//        if ( defaultGatewayMAC == null )
//        {
//            running = false;
//            completed = true;
//            context.onTracerouteCompleted ();
//            return running;
//        }
//
//        TCPPacket tcp = new TCPPacket(
//                localPort,  // int src_port 
//         // int dst_port 
//       // long sequence 
//         // long ack_num 
//                false,      // boolean urg 
//                false,      // boolean ack 
//                false,      // boolean psh 
//                false,      // boolean rst 
//                true,       // boolean syn 
//                false,      // boolean fin 
//                true,       // boolean rsv1 
//                true,       // boolean rsv2 
////         int window
//         // int urgent
//        		);
//        
//        tcp.setIPv4Parameter(
//         // int priority - Priority
//                false,      // boolean: IP flag bit: Delay
//                false,      // boolean: IP flag bit: Through
//                false,      // boolean: IP flag bit: Reliability
//         // int: Type of Service (TOS)
//                false,      // boolean: Fragmentation Reservation flag
//                false,      // boolean: Don't fragment flag
//                false,      // boolean: More fragment flag
//         // int: Offset
//                (int)(Math.random () * 65000), // int: Identifier
//       // int: Time To Live
//                IPPacket.IPPROTO_TCP, // Protocol 
//                localIP,    // Source IP address
//                remoteIP    // Destination IP address
//                );
//        
//        tcp.data=("").getBytes();
//        
//        EthernetPacket ether = new EthernetPacket ();
//        ether.frametype = EthernetPacket.ETHERTYPE_IP;
//        ether.src_mac   = device.mac_address;
//        ether.dst_mac   = defaultGatewayMAC;
//        tcp.datalink    = ether;
//
//        /* Send TCP packets...
//         */
//        JpcapSender sender = captor.getJpcapSenderInstance ();
//        captor.setFilter( "tcp and dst port " + localPort 
//                          + " and dst host " + localIP.getHostAddress(), 
//                          true );
//        
//        for ( int remotePort = rp1; remotePort <= rp2; ++remotePort ) {
//            tcp.src_port = localPort;
//            tcp.dst_port = remotePort;
//            sender.sendPacket( tcp );
//            //println( "SENT: " + tcp );
//        }
//
//        while( running )
//        {
//            TCPPacket p = (TCPPacket)captor.getPacket ();
//
//            if( p == null ) // TIMEOUT
//            {
//                interruptibleSleep( 100 );
//                continue;
//            }
//
//            /* We are here because we got some ICMP packet... resolve name first.
//             */
//            String hopID = p.src_ip.getHostAddress ();
//            if ( resolveNames ) {
//                p.src_ip.getHostName ();
//                hopID = p.src_ip.toString ();
//            }
//
//            // println( "---------------------------------------------------------" );
//            // println( "RECEIVED: " + p.toString () );
//            
//            if ( ! p.rst ) {
//                if ( p.ack_num == 702 ) {
//                    println( "---- " + hopID + " : " + p.src_port );
//                }
//                tcp.dst_port = p.src_port;
//                tcp.fin = p.ack_num == 702 ? true : false;
//                tcp.ack = true;
//                tcp.rst = false;
//                tcp.syn = false;
//                tcp.sequence = p.ack_num;
//                tcp.ack_num = p.sequence + 1;
//                sender.sendPacket( tcp );
//                //println( "SENT: " + tcp );
//            }
//            // running = false;
//        }
//        
//        return running;
//    }
//    
//    /**
//     *  Traces route to given host. The instance is locked during in the mean time
//     *  so other trace routes could not start (completed == false suppresses other 
//     *  threads).
//     */
//    @Override
//    public void run ()
//    {
//        /* Release instance to other threads
//         */
//        if ( ! running ) {
//            completed = true;
//            context.onTracerouteCompleted ();
//            return;
//        }
//
//        /* Make sure that capturing device is configured
//         */
//        if ( captor == null ) {
//            println( "Capturing device is not configured..." );
//            running   = false;
//            completed = true;
//            context.onTracerouteCompleted ();
//            return;
//        }
//
//        /* Starts sending ICMP packets and tracing route...
//         */
//        try
//        {
///*            
//            if ( ! tcpPortScan( 15000, hostName, 1, 1000 ) ) {
//                completed = true;
//                context.onTracerouteCompleted ();
//                return;
//            }
//*/
//            println( "-------------------------------------------------" );
//            print( "Looking up " + hostName + "...", /*timestamp*/ true );
//            
//            InetAddress remoteIP = InetAddress.getByName( hostName );
//            
//            print( "  " + remoteIP.getHostAddress (), /*timestamp*/ false );
//            println ();
//
//            byte[] defaultGatewayMAC = obtainDefaultGatewayMac( "dsv.su.se" );
//            if ( defaultGatewayMAC == null )
//            {
//                running = false;
//                completed = true;
//                context.onTracerouteCompleted ();
//                return;
//            }
//  
//            if ( initialTTL == 0 ) {
//                println( "Tracing route to " + remoteIP + "..." );
//            } else {
//                println( "Pinging host " + remoteIP + "..." );
//            }
//            
//            /* Create ICMP packet
//             */
//            ICMPPacket icmp = new ICMPPacket ();
//
//            icmp.type       = ICMPPacket.ICMP_ECHO;
//            icmp.seq        = 100;
//            icmp.id         = 0;
//            icmp.data       = "data".getBytes ();
//            
//            icmp.setIPv4Parameter(
//         // int priority - Priority
//                    false,      // boolean: IP flag bit: Delay
//                    false,      // boolean: IP flag bit: Through
//                    false,      // boolean: IP flag bit: Reliability
//         // int: Type of Service (TOS)
//                    false,      // boolean: Fragmentation Reservation flag
//                    false,      // boolean: Don't fragment flag
//                    false,      // boolean: More fragment flag
//         // int: Offset
//         // int: Identifier
//        // int: Time To Live
//                    IPPacket.IPPROTO_ICMP, // Protocol 
//                    localIP,    // Source IP address
//                    remoteIP    // Destination IP address
//                    );
//
//            EthernetPacket ether = new EthernetPacket ();
//            ether.frametype = EthernetPacket.ETHERTYPE_IP;
//            ether.src_mac   = device.mac_address;
//            ether.dst_mac   = defaultGatewayMAC;
//            icmp.datalink   = ether;
//           
//            /* Send ICMP packets...
//             */
//            JpcapSender sender = captor.getJpcapSenderInstance ();
//            captor.setFilter( "icmp and dst host " + localIP.getHostAddress(), true );
//            
//            icmp.hop_limit  = (short)initialTTL;
//            print( icmp.hop_limit + ": ", /*timestamp*/ true );
//            int timeoutTimer = 0;
//            int timeoutCounter = 0;
//            long tStart = System.nanoTime ();
//            sender.sendPacket( icmp );
//            
//            while( running )
//            {
//                ICMPPacket p = (ICMPPacket)captor.getPacket ();
//                int tDelay = (int)( ( System.nanoTime () - tStart ) / 1000000l );
//
//                if( p == null ) // TIMEOUT
//                {
//                    /* Continue waiting until ~2 sec elapses
//                     */
//                    if ( timeoutTimer < 30 ) 
//                    {
//                        interruptibleSleep( timeoutTimer < 10 ? 1 : 100 );
//                        ++timeoutTimer;
//                        if ( timeoutTimer >= 10 ) {
//                            print( ".", /*timestamp*/ false );
//                        }
//                        continue;
//                    }
//
//                    /* Increase timeout counter and either retry or advance Hop limit
//                     */
//                    ++timeoutCounter;
//                    print( " Timeout #" + timeoutCounter, /*timestamp*/ false );
//                    
//                    if ( timeoutCounter < 3 ) // Retry send to the same Hop
//                    {
//                        print( icmp.hop_limit + ": ", /*timestamp*/ true );
//                        tStart = System.nanoTime ();
//                        timeoutTimer = 0;
//                        sender.sendPacket( icmp );
//                    }
//                    else // Advance Hop limit and send to next hop
//                    {
//                        ++icmp.hop_limit;
//                        print( icmp.hop_limit + ": ", /*timestamp*/ true );
//                        timeoutTimer = 0;
//                        timeoutCounter = 0;
//                        tStart = System.nanoTime ();
//                        sender.sendPacket( icmp );
//                    }
//                    continue;
//                }
//                
//                /* We are here because we got some ICMP packet... resolve name first.
//                 */
//                String hopID = p.src_ip.getHostAddress ();
//                if ( resolveNames ) {
//                    p.src_ip.getHostName ();
//                    hopID = p.src_ip.toString ();
//                }
//
//                /* Now, in case if we received 'time exceeded' packet we should advance
//                 * to the next Hop limit. Otherwise if host is either unreachable or 
//                 * we got echo reply, we should quit.
//                 */
//                if( p.type == ICMPPacket.ICMP_TIMXCEED ) // Time exceeded
//                {
//                    print( hopID + ", " + tDelay + " ms", /*ts*/ false  );
//                    ++icmp.hop_limit;
//                    print( icmp.hop_limit + ": ", /*timestamp*/ true );
//                    timeoutTimer = 0;
//                    timeoutCounter = 0;
//                    tStart = System.nanoTime ();
//                    sender.sendPacket( icmp );
//                }
//                else if( p.type == ICMPPacket.ICMP_UNREACH ) // Host unreachable
//                {
//                    print( hopID + " unreachable", /*ts*/ false  );
//                    running = false;
//                }
//                else if( p.type == ICMPPacket.ICMP_ECHOREPLY ) // Echo reply (pong)
//                {
//                    print( hopID + ", " + tDelay + " ms", /*ts*/ false );
//                    if ( initialTTL != 0 ) {
//                        println( hopID + " is alive." );
//                    }
//                    running = false;
//                }
//            }
//        }
//        catch( UnknownHostException e )
//        {
//            println( "Unknown host: " + hostName );
//            completed = true;
//            context.onTracerouteCompleted ();
//            return;
//        }
//        catch( IOException e )
//        {
//            println( "ERROR: " + e.toString () );
//            completed = true;
//            context.onTracerouteCompleted ();
//            return;
//        }
//
//        /* Release instance to other threads
//         */
//        println( initialTTL == 0 ? "Traceroute completed." : "Ping completed." );
//        completed = true;
//        context.onTracerouteCompleted ();
//     }
//}