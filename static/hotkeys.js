
// Keys 1-9 can be used to select radio buttons or checkboxes with class="hotkey".

// This feature is turned off while editing text in a textfield or textarea.

// jQuery version 1.4.2 should be loaded before this script.

$(document).ready(function(){
	// Only do hotkeys if we have enough keys for all.
	if($('span.hotkey').length < 10) {
		// Handle keydown event for keys 1-9.
		$(document).keydown(function(event) {
			if(event.keyCode > 48 && event.keyCode < 58 
				&& $('.textfocus').length === 0) 
			{
				$("input.hotkey").each(function(i) {
					if(i == event.keyCode - 49) {
						$(this).click();
					}
				});
			}
		});
		// Add labels so we know which numbers select which radio buttons or checkboxes.
		$('span.hotkey').text(function(i,t) {
			if(i < 9) {
				return t + ' (' + (i+1) + ')';
			} else {
				return t;
			}
		});
		// Mark text input fields with the textfocus class when they are in focus.
		$('input[type=text],textarea')
		.blur(function() {
			$(this).removeClass("textfocus");
		})
		.focus(function() {
			$(this).addClass("textfocus");
		});
	}
});

