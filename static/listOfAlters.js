
<!-- listOfAlters.js                                   -->
<!-- javaScript functions specific to keyboard control -->
<!-- of the list-of-alters page                        -->
<!-- KCN May 29 2010                                   -->

var loaRowList = null;
var loaColList = null;
var loaCurrentRow = null;
var iloaCurrentRow = null;
var iloaCurrentCol = null;
var loaInitialized = false;

//============================================================
// loaInitializeHorz
//============================================================

function loaInitialize(horizontal) {

    var iy = 0;

    if ( !loaInitialized ) {
        loaRowList = new Array();
        tempList = document.getElementsByTagName("div");

        if ( horizontal ) {
            for ( ix=0 ; ix<tempList.length ; ++ix ) {
                if ( tempList[ix].id != null && tempList[ix].id.match("horizontalForm*")) {
                    loaRowList[iy] = tempList[ix];
                    ++iy;
                }
            }
        } else {
             for ( ix=0 ; ix<tempList.length ; ++ix ) {
                if ( tempList[ix].id != null && tempList[ix].id.match("verticalForm*")) {
                    loaRowList[iy] = tempList[ix];
                    ++iy;
                }
            }
        }

        iloaCurrentRow = 0;
        iloaCurrentCol = 0;
        loaCurrentRow = loaRowList[iloaCurrentRow];
        if ( horizontal )
            loaCurrentRow.className = "loaHiliteRow";

        loaColList = loaCurrentRow.getElementsByTagName("input");
        loaColList[iloaCurrentCol].focus();
        loaColList[iloaCurrentCol].className = "loaHiliteItem";
    }
    loaInitialized = true;
}

//===========================================================
// loaIsInRowList
//===========================================================

function loaIsInRowList ( object ) {

   for ( ix=0 ; ix<loaRowList.length ; ++ix ) {
       if ( loaRowList[ix] == object )
            return (true);
   }
   return(false);
}

//===========================================================
// loaNextInRowList
//===========================================================

function loaNextInRowList ( object, forward ) {
    
    var iLength = loaRowList.length;

    for ( ix=0 ; ix<iLength ; ++ix ) {
        if ( loaRowList[ix] == object ) {
            if ( forward )
                return ( loaRowList[(ix+1)%iLength] );
            else
                return ( loaRowList[(ix+iLength-1)%iLength]);
        }
    }
   return(null);
}


//===========================================================
// loaFindAncestorOnRowList
//===========================================================

function loaFindAncestorOnRowList ( object ) {

	if ( object == null )
          return(object);

    thisParent = object.parentNode;

      while ( thisParent !=null ) {
           found = loaIsInRowList(thisParent );
           if ( found )
               return (thisParent);
           thisParent = thisParent.parentNode;
      }
      return(null);
}

//========================================================
// loaFindInColList
//========================================================

function loaFindInColList ( object ) {
    
    for ( ix=0 ; ix<loaColList.length ; ++ix )  {
        if (loaColList[ix] == object ) 
            return(ix);
    }
    return(0);
}


//========================================================
// doOnFocusHorz
//========================================================

function doOnFocusHorz(object) {

      var ix;
      var init;

      init = loaInitialized;
      if ( !loaInitialized ) 
          loaInitialize(true);

      object.className = "loaHiliteItem";

      thisParent = loaFindAncestorOnRowList(object);

      if ( thisParent != loaCurrentRow  ||  !init ) {
          if ( loaCurrentRow != null ) {
              loaCurrentRow.className = "loaNormalRow";
              for ( ix=0 ; ix<loaColList.length ; ++ix )
                  loaColList[ix].className = "loaNormalRow";
		}
	     loaCurrentRow = thisParent ;

         loaCurrentRow.className = "loaHiliteRow";
      }
    loaColList = loaCurrentRow.getElementsByTagName("input");
    iloaCurrentCol = loaFindInColList(object);
    loaColList[iloaCurrentCol].focus();
    loaColList[iloaCurrentCol].className = "loaHiliteItem";
}

//========================================================
// doOnFocusVert
//========================================================

function doOnFocusVert(object) {

      var ix;
      var init;

      init = loaInitialized;
      if ( !loaInitialized )
          loaInitialize(false);

      object.className = "loaHiliteItem";

      thisParent = loaFindAncestorOnRowList(object);

      if ( thisParent != loaCurrentRow  ||  !init ) {
          if ( loaCurrentRow != null ) {
              loaCurrentRow.className = "loaNormalRow";
              for ( ix=0 ; ix<loaColList.length ; ++ix )
                  loaColList[ix].className = "loaNormalRow";
		}
	     loaCurrentRow = thisParent ;

         // loaCurrentRow.className = "loaHiliteRow";
      }
    loaColList = loaCurrentRow.getElementsByTagName("input");
    iloaCurrentCol = loaFindInColList(object);
    loaColList[iloaCurrentCol].focus();
    loaColList[iloaCurrentCol].className = "loaHiliteItem";
}

//===========================================================
// doOnBlur
//===========================================================

function doOnBlur(object) {

    object.className = "loaNormalRow";
}

//===========================================================
// doOnKeyUpHorz
// when checkboxes are laid out horizontally they are stacked
// on on top of each other, each row is a grouping of checkboxes
// for one alter, the column indicating different checkboxes
// within that row
//===========================================================

function doOnKeyUpHorz(event) {
     var nextCol = iloaCurrentCol;
     var nextDiv = loaCurrentRow;
     var unicode=event.keyCode? event.keyCode : event.charCode;

     if ( unicode==38 ) {
         nextDiv = loaNextInRowList ( loaCurrentRow, false );
     } else if ( unicode==40 ) {
         nextDiv = loaNextInRowList ( loaCurrentRow, true);
     } else if ( unicode==39 ) {
         nextCol = (iloaCurrentCol+1) % loaColList.length;
     } else if ( unicode==37 ) {
         nextCol = iloaCurrentCol-1;
         if ( nextCol<0 ) 
             nextCol = loaColList.length-1;
     }

      if ( nextDiv!=null  &&  nextDiv != loaCurrentRow ) {
          if ( loaCurrentRow != null ) 
              loaCurrentRow.className = "loaNormalRow";
	    loaCurrentRow = nextDiv ;
          loaCurrentRow.className = "loaHiliteRow"; 
  	    loaColList = loaCurrentRow.getElementsByTagName("input");
          if ( iloaCurrentCol >= loaColList.length )
              iloaCurrentCol = loaColList.length-1;
          loaColList[iloaCurrentCol].focus();
	    loaColList[iloaCurrentCol].classname = "loaHiliteItem";
          // event.preventDefault();
          event.returnValue = false;
      } else if ( nextCol!=iloaCurrentCol ) {
          iloaCurrentCol = nextCol;
          loaColList[iloaCurrentCol].focus();
          loaColList[iloaCurrentCol].className = "loaHiliteItem";
	    // event.preventDefault();
          event.returnValue = false;
      } else {
          event.returnValue = true;
      }
}

//===========================================================
// doOnKeyUpVert
// when the list of checkboxes is vertical, we no longer
// have what were once 'rows', and what was previously
// the column is now the 'row' index
//===========================================================

function doOnKeyUpVert(event) {
     var nextCol = iloaCurrentCol;
     var unicode=event.keyCode? event.keyCode : event.charCode;

     if ( unicode==38 || unicode==37 ) {
         nextCol = iloaCurrentCol-1;
         if ( nextCol<0 )
             nextCol = loaColList.length-1;
     } else if ( unicode==40 || unicode==39 ) {
         nextCol = (iloaCurrentCol+1) % loaColList.length;
     }

      if ( nextCol!=iloaCurrentCol ) {
          iloaCurrentCol = nextCol;
          loaColList[iloaCurrentCol].focus();
          loaColList[iloaCurrentCol].className = "loaHiliteItem";
          // event.preventDefault();
          // event.stopPropagation();
          event.returnValue = false;
      } else {
          event.returnValue = true;
      }
}