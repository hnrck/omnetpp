#==========================================================================
#  MAIN.TCL -
#            part of the Tcl/Tk windowing environment of
#                            OMNeT++
#==========================================================================

#----------------------------------------------------------------#
#  Copyright (C) 1992-2001 Andras Varga
#  Technical University of Budapest, Dept. of Telecommunications,
#  Stoczek u.2, H-1111 Budapest, Hungary.
#
#  This file is distributed WITHOUT ANY WARRANTY. See the file
#  `license' for details on this and other legal matters.
#----------------------------------------------------------------#

#------
# Parts of this file were created using Stewart Allen's Visual Tcl (vtcl)
#------

#===================================================================
#    GLOBAL VARIABLES
#===================================================================

# default config settings
set config(editor-findstring) ""
set config(editor-case-sensitive) 0
set config(editor-whole-words) 0
set config(editor-regexp) 0
set config(editor-backwards) 0


#===================================================================
#    MAIN OMNET++ WINDOW
#===================================================================

proc create_omnetpp_window {} {

    global icons fonts tcl_version help_tips

    wm focusmodel . passive
    wm geometry . 540x275
    wm maxsize . 1009 738
    wm minsize . 1 1
    wm overrideredirect . 0
    wm resizable . 1 1
    wm deiconify .
    wm title . "OMNeT++/Tkenv"
    wm protocol . WM_DELETE_WINDOW {exit_omnetpp}

    #################################
    # Menu bar
    #################################

    if {$tcl_version < 8.0} {
        frame .menubar -borderwidth 1 -relief raised
        pack .menubar -expand 0 -fill x -side top
    } else {
        menu .menubar
        . config -menu .menubar
    }

    # Create menus
    foreach i {
       {filemenu     -$label_opt File -underline 0}
       {editmenu     -$label_opt Edit -underline 0}
       {simulatemenu -$label_opt Simulate -underline 0}
       {tracemenu    -$label_opt Trace -underline 0}
       {inspectmenu  -$label_opt Inspect -underline 0}
       {viewmenu     -$label_opt View -underline 0}
       {optionsmenu  -$label_opt Options -underline 0}
       {helpmenu     -$label_opt Help -underline 0}
    } {
       if {$tcl_version < 8.0} {
           set label_opt "text"; set m ".m"
           set mb [eval menubutton .menubar.$i -padx 4 -pady 3]
           menu $mb.m -tearoff 0
           $mb config -menu $mb.m
       } else {
           set label_opt "label"; set m ""
           eval .menubar add cascade -menu .menubar.$i
           menu ".menubar.[lindex $i 0]" -tearoff 0
       }
    }

    # File menu
    foreach i {
      {command -command new_run -label {Select Run #...} -underline 7}
      {command -command new_network -label {Select network...} -underline 7}
      {command -command create_snapshot -label {Create snapshot...} -underline 7}
      {separator}
      {command -command exit_omnetpp -label Exit -underline 1}
    } {
      eval .menubar.filemenu$m add $i
    }

    # Edit menu
    foreach i {
      {command -command edit_find -label {Find...} -accel {Ctrl-F} -underline 0}
      {command -command edit_findnext -label {Find next} -accel {Ctrl-N,F3} -underline 5}
    } {
      eval .menubar.editmenu$m add $i
    }

    # Simulate menu
    #  {command -command init_step -label {First real event} -underline 1}
    #  {separator}
    foreach i {
      {command -command one_step -label {One step} -accel {F4} -underline 4}
      {command -command slow_exec -label {Slow execution} -underline 1}
      {separator}
      {command -command run -label {Run}  -accel {F5} -underline 0}
      {command -command run_fast -label {Fast run (rare display updates)} -accel {F6} -underline 0}
      {command -command run_express -label {Express run (tracing off)} -accel {F7} -underline 0}
      {command -command run_until -label {Run until...} -underline 4}
      {separator}
      {command -command stop_simulation -label {Stop execution} -accel {F8} -underline 0}
      {separator}
      {command -command call_finish -label {Call finish() for all modules} -underline 0}
      {command -command rebuild -label {Rebuild network} -underline 1}
    } {
      eval .menubar.simulatemenu$m add $i
    }

    # Trace menu
    foreach i {
      {command -command module_windows -label {Module output...} -underline 0}
      {command -command message_windows -label {Message sending...} -underline 8}
      {separator}
      {command -command clear_windows -label {Clear main window} -underline 0}
    } {
      eval .menubar.tracemenu$m add $i
    }

    # Inspect menu
    foreach i {
      {command -command inspect_systemmodule -label {Network} -underline 0}
      {command -command inspect_messagequeue -label {Scheduled events (FES)} -underline 0}
      {separator}
      {cascade -label {Linked-in components} -underline 10 -menu .menubar.inspectmenu$m.components}
      {separator}
      {command -command inspect_anyobject -label {From list of all objects...} -underline 0}
      {command -command inspect_matching -label {By pattern matching...} -underline 3}
      {command -command inspect_bypointer -label {By pointer...} -underline 4}
    } {
      eval .menubar.inspectmenu$m add $i
    }

    # Inspect|Components menu
    menu .menubar.inspectmenu$m.components -tearoff 0
    foreach i {
      {command -command inspect_networks -label {Available networks} -underline 10}
      {command -command inspect_moduletypes -label {Module types} -underline 0}
      {command -command inspect_channeltypes -label {Channel types} -underline 0}
      {command -command inspect_functions -label {Registered functions} -underline 11}
    } {
      eval .menubar.inspectmenu$m.components add $i
    }

    # Inspect|Special menu
    #  {cascade -label {Special} -underline 1 -menu .menubar.inspectmenu$m.special}
    # menu .menubar.inspectmenu$m.special -tearoff 0
    # foreach i {
    #  {command -command inspect_anyobject -label {From list of all objects...} -underline 0}
    #  {command -command inspect_matching -label {By pattern matching...} -underline 3}
    #  {command -command inspect_bypointer -label {By pointer...} -underline 4}
    #} {
    #  eval .menubar.inspectmenu$m.special add $i
    #}

    # View menu
    foreach i {
      {command -label {Ini file} -underline 0 -command view_inifile}
      {command -label {README} -underline 0 -command {view_file README}}
      {separator}
      {command -label {Output vector file} -underline 0 -command view_outputvectorfile}
      {command -label {Output scalar file} -underline 0 -command view_outputscalarfile}
      {command -label {Snapshot file} -underline 0 -command view_snapshotfile}
      {command -label {Inspector list file} -underline 1 -command view_inspectorlistfile}
      {separator}
      {command -label {View/Edit file...} -underline 0 -command {edit_textfile}}
    } {
      eval .menubar.viewmenu$m add $i
    }

    # Options menu
    foreach i {
      {command -command simulation_options -label {Simulation options...} -underline 0}
      {separator}
      {command -label {Load inspector list...} -underline 0 -command load_inspectorlist}
      {command -label {Save open inspectors list...} -underline 0 -command save_inspectorlist}
      {command -label {Update inspectors now} -underline 0 -command opp_updateinspectors}
      {separator}
      {command -command show_console -label {TCL console} -underline 0}
    } {
      eval .menubar.optionsmenu$m add $i
    }

    # Help menu
    foreach i {
      {command -command about -label {About OMNeT++/Tkenv} -underline 0}
    } {
      eval .menubar.helpmenu$m add $i
    }

    # Pack menus on menubar
    if {$tcl_version < 8.0} {
        foreach i {
          filemenu editmenu simulatemenu tracemenu inspectmenu viewmenu optionsmenu helpmenu
        } {
          pack .menubar.$i -anchor n -expand 0 -fill none -side left
        }
    }

    #################################
    # Create toolbar
    #################################

    frame .toolbar -relief raised -borderwidth 1
    foreach i {
      {sep0     -separator}
      {step     -image $icons(step)    -command {one_step}}
      {sep1     -separator}
      {run      -image $icons(run)     -command {run}}
      {fastrun  -image $icons(fast)    -command {run_fast}}
      {exprrun  -image $icons(express) -command {run_express}}
      {sep2     -separator}
      {until    -image $icons(until)   -command {run_until}}
      {sep3     -separator}
      {stop     -image $icons(stop)    -command {stop_simulation}}
      {sep4     -separator}
      {network  -image $icons(network) -command {inspect_systemmodule}}
      {fes      -image $icons(fes)     -command {inspect_messagequeue}}
    } {
      set b [eval iconbutton .toolbar.$i]
      pack $b -anchor n -expand 0 -fill none -side left -padx 0 -pady 2
    }

    set help_tips(.toolbar.step)    {Execute one event (F4)}
    set help_tips(.toolbar.run)     {Run with full animation (F5)}
    set help_tips(.toolbar.fastrun) {Run faster: no animation and rare inspector updates (F6)}
    set help_tips(.toolbar.exprrun) {Run at full speed: no text output, animation or inspector updates (F7)}
    set help_tips(.toolbar.until)   {Run until time or event number}
    set help_tips(.toolbar.stop)    {Stop simulation if running (F8)}
    set help_tips(.toolbar.network) {Inspect network}
    set help_tips(.toolbar.fes)     {Inspect Future Event Set}

    #################################
    # Create status bars
    #################################

    frame .statusbar
    frame .statusbar2
    frame .statusbar3

    label .statusbar.networklabel \
        -relief groove -text {(no network set up)} -width 18 -anchor w
    label .statusbar.eventlabel \
        -relief groove -text {Event #0} -width 15  -anchor w
    label .statusbar.timelabel \
        -relief groove -text {T=0.0000000 (0.00s)} -width 20 -anchor w
    label .statusbar.nextlabel \
        -relief groove -text {Next:} -width 23 -anchor w

    label .statusbar2.feslength \
        -relief groove -text {Msgs in FES: 0} -width 20 -anchor w
    label .statusbar2.totalmsgs \
        -relief groove -text {Total msgs: 0} -width 20 -anchor w
    label .statusbar2.livemsgs \
        -relief groove -text {Live msgs: 0} -width 20 -anchor w

    label .statusbar3.eventspersec \
        -relief groove -text {Ev/sec: n/a} -width 15 -anchor w
    label .statusbar3.simsecpersec \
        -relief groove -text {Simsec/sec: n/a} -width 22 -anchor w
    label .statusbar3.eventspersimsec \
        -relief groove -text {Ev/simsec: n/a} -width 18 -anchor w

    pack .statusbar.networklabel -anchor n -expand 1 -fill x -side left
    pack .statusbar.eventlabel -anchor n -expand 1 -fill x -side left
    pack .statusbar.timelabel -anchor n -expand 1 -fill x -side left
    pack .statusbar.nextlabel -anchor n -expand 1 -fill x -side right

    pack .statusbar2.feslength -anchor n -expand 1 -fill x -side left
    pack .statusbar2.totalmsgs -anchor n -expand 1 -fill x -side left
    pack .statusbar2.livemsgs -anchor n -expand 1 -fill x -side left

    pack .statusbar3.eventspersec -anchor n -expand 1 -fill x -side left
    pack .statusbar3.simsecpersec -anchor n -expand 1 -fill x -side left
    pack .statusbar3.eventspersimsec -anchor n -expand 1 -fill x -side left


    #################################
    # Create main display area
    #################################

    frame .main -borderwidth 1 -height 30 -relief sunken -width 30
    text .main.text -yscrollcommand ".main.sb set"
    scrollbar .main.sb -command ".main.text yview"
    .main.text tag configure event -foreground blue
    .main.text tag configure SELECT -back #808080 -fore #ffffff

    pack .main.sb -anchor center -expand 0 -fill y  -side right
    pack .main.text -anchor center -expand 1 -fill both -side left

    #################################
    # Pack everything
    #################################
    if {$tcl_version < 8.0} {
        pack .menubar   -anchor center -expand 0 -fill x -side top
    }
    pack .toolbar    -anchor center -expand 0 -fill x -side top
    pack .statusbar  -anchor center -expand 0 -fill x -side top
    pack .statusbar2 -anchor center -expand 0 -fill x -side top
    pack .statusbar3 -anchor center -expand 0 -fill x -side top
    pack .main       -anchor center -expand 1 -fill both -side top

    global tcl_platform
    if {$tcl_platform(platform) == "windows"} {
        update
    }

    focus .main.text

    ###############################
    # Hotkeys
    ###############################
    bind .main.text <Key> {%W tag remove SELECT 0.0 end}

    # bindings for find
    #   'break' is needed below because
    #      ^F is originally bound to 1 char right
    #      ^N is originally bound to 1 line down
    bind .main.text <Control-f> {findDialog .main.text;break}
    bind .main.text <Control-F> {findDialog .main.text;break}
    bind .main.text <Control-n> {findNext .main.text;break}
    bind .main.text <Control-N> {findNext .main.text;break}
    bind .main.text <F3> {findNext .main.text}

    bind_runcommands .
}

proc bind_runcommands {w} {
    bind $w <F4> {one_step}
    bind $w <F5> {run}
    bind $w <F6> {run_fast}
    bind $w <F7> {run_express}
    bind $w <F8> {stop_simulation}
}


#===================================================================
#    LOAD BITMAPS
#===================================================================

proc load_bitmaps {path} {
   global tcl_platform

   puts "Loading bitmaps from $path:"

   # On Windows, we use ";" to separate directories in $path. Using ":" (the
   # Unix separator) would cause trouble with dirs containing drive letter
   # (like "c:\bitmaps"). Using a space is also not an option (think of
   # "C:\Program Files\...").

   if {$tcl_platform(platform) == "unix"} {
       set sep {:;}
   } else {
       set sep {;}
   }

   set files {}
   foreach dir [split $path $sep] {
       set files [concat $files \
                     [glob -nocomplain -- [file join $dir {*.gif}]] \
                     [glob -nocomplain -- [file join $dir {*.xpm}]] \
                     [glob -nocomplain -- [file join $dir {*.xbm}]]]
   }
   if {[llength $files] == 0} {
       puts "*** no bitmaps (gif,xpm,xbm) in $path"
       return
   }

   foreach f $files {

      set type ""
      case [string tolower [file extension $f]] {
        {.xbm} {set type bitmap}
        {.xbm} {set type photo}
        {.gif} {set type photo}
      }
      if {$type==""} {error "load_bitmaps: internal error"}

      set name [string tolower [file tail [file rootname $f]]]
      if {$name=="proc"} {
         puts -nonewline "(*** $name -- Tk dislikes this name, skipping ***) "
      } elseif [catch {image type $name}] {
         if [catch {
            image create $type $name -file $f
            puts -nonewline "$name "
         } err] {
            puts -nonewline "(*** $name is bad: $err ***) "
         }
      } else {
         puts -nonewline "($name is duplicate) "
      }
   }
   puts ""
   puts ""
}


#===================================================================
#    LOAD PLUGINS
#===================================================================

proc load_plugins {path} {
   global tcl_platform

   puts "Loading plugins from $path:"

   # On Windows, we use ";" to separate directories in $path. Using ":" (the
   # Unix separator) would cause trouble with dirs containing drive letter
   # (like "c:\bitmaps"). Using a space is also not an option (think of
   # "C:\Program Files\...").

   if {$tcl_platform(platform) == "unix"} {
       set dllpattern "*.so*"
       set sep {:;}
   } else {
       set dllpattern "*.dll"
       set sep {;}
   }

   set tclfiles 0
   set dllfiles 0
   foreach dir [split $path $sep] {

      # load tcl files
      set files [lsort [glob -nocomplain -- [file join $dir {*.tcl}]]]
      incr tclfiles [llength $files]
      foreach file $files {
         if [catch {source $file} errmsg] {
             puts ""
             puts "*** error sourcing $file: $errmsg"
         } else {
             set name [string tolower [file tail $file]]
             puts -nonewline "$name "
         }
      }

      # load dynamic libraries
      set files [lsort [glob -nocomplain -- [file join $dir $dllpattern]]]
      incr dllfiles [llength $files]
      foreach file $files {
         if [catch {opp_loadlib $file} errmsg] {
             puts ""
             puts "*** error loading shared library $file: $errmsg"
         } else {
             set name [string tolower [file tail $file]]
             puts -nonewline "$name "
         }
      }
      if {$dllfiles!=0} {puts ""}
   }
   if {$tclfiles!=0 || $dllfiles!=0} {puts ""}

   if {$tclfiles==0} {
      puts "  no *.tcl file in $path"
   }
   if {$dllfiles==0} {
      puts "  no $dllpattern file in $path"
   }
}

#===================================================================
#    GENERIC BINDINGS
#===================================================================

proc generic_bindings {} {
   bind Button <Return> {tkButtonInvoke %W}
}

#===================================================================
#    FONT BINDINGS
#===================================================================

proc font_bindings {} {
   global fonts tcl_platform tk_version

   #
   # fonts() array elements:
   #  normal:  menus, labels etc
   #  bold:    buttons
   #  icon:    toolbar 'icons'
   #  big:     STOP button
   #  msgname: message name during animation
   #  fixed:   text windows and listboxes
   #

   if {$tcl_platform(platform) == "unix"} {
      set fonts(normal)  -Adobe-Helvetica-Medium-R-Normal-*-*-120-*-*-*-*-*-*
      set fonts(bold)    -Adobe-Helvetica-Bold-R-Normal-*-*-120-*-*-*-*-*-*
      set fonts(icon)    -Adobe-Times-Bold-I-Normal-*-*-120-*-*-*-*-*-*
      set fonts(big)     -Adobe-Helvetica-Medium-R-Normal-*-*-180-*-*-*-*-*-*
      set fonts(msgname) -Adobe-Helvetica-Medium-R-Normal-*-*-120-*-*-*-*-*-*
      set fonts(fixed)   fixed
      set fonts(balloon) -Adobe-Helvetica-Medium-R-Normal-*-*-120-*-*-*-*-*-*
   } else {
      # Windows, Mac
      if {$tk_version<8.2} {
         set s 140
      } else {
         set s 110
      }
      font create opp_normal -family "MS Sans Serif" -size 8
      font create opp_bold   -family "MS Sans Serif" -size 8 -weight bold
      font create opp_balloon -family "MS Sans Serif" -size 8

      set fonts(normal)  opp_normal
      set fonts(bold)    opp_bold
      set fonts(icon)    -Adobe-Helvetica-Bold-R-Normal-*-*-$s-*-*-*-*-*-*
      set fonts(big)     -Adobe-Helvetica-Medium-R-Normal-*-*-180-*-*-*-*-*-*
      set fonts(msgname) -Adobe-Helvetica-Medium-R-Normal-*-*-$s-*-*-*-*-*-*
      set fonts(fixed)   FixedSys
      set fonts(balloon) opp_balloon
   }

   if {$tcl_platform(platform) == "unix"} {
       option add *Scrollbar.width  12
       option add *Menubutton.font  $fonts(normal)
       option add *Menu.font        $fonts(normal)
       option add *Label.font       $fonts(normal)
       option add *Entry.font       $fonts(normal)
       option add *Listbox.font     $fonts(fixed)
       option add *Text.font        $fonts(fixed)
       option add *Button.font      $fonts(bold)
   }
}

proc checkVersion {} {

   global tk_version tk_patchLevel

   catch {package require Tk}
   if {$tk_version<8.0} {
      wm deiconify .
      wm title . "Bad news..."
      frame .f
      pack .f -expand 1 -fill both -padx 2 -pady 2
      label .f.l1 -text "Your version of Tcl/Tk is too old!"
      label .f.l2 -text "Tcl/Tk 8.0 or later required."
      button .f.b -text "OK" -command {exit}
      pack .f.l1 .f.l2 -side top -padx 5
      pack .f.b -side top -pady 5
      focus .f.b
      wm protocol . WM_DELETE_WINDOW {exit}
      tkwait variable ok
   }
   if {$tk_patchLevel=="8.0"} {
      tk_messageBox -title {Warning} -type ok -icon warning \
        -message {Old Tcl/Tk version. At least 8.0p1 is strongly recommended!}
   }
}


proc startup_commands {} {

    set run [opp_getsimoption default-run]
    if {$run>0} {
        opp_newrun $run

        if {[opp_object_systemmodule] != [opp_object_nullpointer]} {
            opp_inspect [opp_object_systemmodule] (default)
            # tk_messageBox -title {OMNeT++} -message {[Run 1] set up.} \
                            -type ok -icon info
        }
    } else {
        new_run
    }

}

