# Comprehensive Optimization Algorithm Comparison

**Test Date:** 2026-01-23 19:41:49

## Algorithm Implementations

- **Level 0: Greedy** - Fast greedy heuristic
- **Level 1: Beam Search** - Explores multiple paths (beam width=16)
- **Level 2: Branch & Bound A*** - Exhaustive search with pruning (5s timeout)

---


## Scenario: simple (maxDelay=100)

```
======================================
Level 0:
--------------------------------------
Nice file! 3 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 60
Stock :
client_content=> 1
euro=> 2
materiel=> 0
produit=> 0

======================================
Level 1:
--------------------------------------
Nice file! 3 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 60
Stock :
client_content=> 1
euro=> 2
materiel=> 0
produit=> 0

======================================
Level 2:
--------------------------------------
Nice file! 3 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 60
Stock :
client_content=> 1
euro=> 2
materiel=> 0
produit=> 0

```


## Scenario: ikea (maxDelay=150)

```
======================================
Level 0:
--------------------------------------
Nice file! 4 processes, 5 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 30
Stock :
etagere=> 1
fond=> 2
montant=> 2
planche=> 0

======================================
Level 1:
--------------------------------------
Nice file! 4 processes, 5 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 10
Stock :
etagere=> 7
planche=> 0

======================================
Level 2:
--------------------------------------
Nice file! 4 processes, 5 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 50
Stock :
armoire=> 1
etagere=> 0
fond=> 0
montant=> 0
planche=> 0

```


## Scenario: steak (maxDelay=200)

```
======================================
Level 0:
--------------------------------------
Nice file! 5 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 40
Stock :
poele=> 1
steak_cru=> 0
steak_cuit=> 3
steak_mi_cuit=> 0

======================================
Level 1:
--------------------------------------
Nice file! 5 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 30
Stock :
poele=> 1
steak_cru=> 0
steak_cuit=> 3
steak_mi_cuit=> 0

======================================
Level 2:
--------------------------------------
Nice file! 5 processes, 4 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 30
Stock :
poele=> 1
steak_cru=> 0
steak_cuit=> 3
steak_mi_cuit=> 0

```


## Scenario: recre (maxDelay=150)

```
======================================
Level 0:
--------------------------------------
Nice file! 4 processes, 3 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 150
Stock :
bonbon=> 4
marelle=> 2
moi=> 1

======================================
Level 1:
--------------------------------------
Nice file! 4 processes, 3 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 150
Stock :
bonbon=> 22
moi=> 1

======================================
Level 2:
--------------------------------------
Nice file! 4 processes, 3 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 150
Stock :
bonbon=> 1
marelle=> 3
moi=> 1

```


## Scenario: inception (maxDelay=200)

```
======================================
Level 0:
--------------------------------------
Nice file! 11 processes, 7 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 200
Stock :
clock=> 1
minute=> 3
second=> 21

======================================
Level 1:
--------------------------------------
Nice file! 11 processes, 7 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 200
Stock :
clock=> 1
second=> 201

======================================
Level 2:
--------------------------------------
Nice file! 11 processes, 7 stocks, 1 to optimize
Evaluating .................. done.
no more process doable at time 192
Stock :
clock=> 0
dream=> 2
hour=> 5
minute=> 0
second=> 0

```


## Scenario: pomme (maxDelay=200)

```
======================================
Level 0:
--------------------------------------
Nice file! 18 processes, 16 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 200
Stock :
beurre=> 3986
blanc_oeuf=> 4
citron=> 800
euro=> 8800
farine=> 1300
four=> 10
jaune_oeuf=> 1
lait=> 3993
oeuf=> 191
pate_feuilletee=> 100
pate_sablee=> 300
pomme=> 1400

======================================
Level 1:
--------------------------------------
Nice file! 18 processes, 16 stocks, 1 to optimize
Evaluating .................. done.
no more process doable at time 200
Stock :
euro=> 0
four=> 10
pomme=> 70000

======================================
Level 2:
--------------------------------------
Nice file! 18 processes, 16 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 200
Stock :
beurre=> 3986
blanc_oeuf=> 4
citron=> 800
euro=> 8800
farine=> 1300
four=> 10
jaune_oeuf=> 1
lait=> 3993
oeuf=> 191
pate_feuilletee=> 100
pate_sablee=> 300
pomme=> 1400

```


## Scenario: coffee_shop (maxDelay=500)

```
======================================
Level 0:
--------------------------------------
Nice file! 13 processes, 12 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 281
Stock :
box_pastries=> 0
butter=> 90
coffee=> 105
coffee_beans=> 0
croissant=> 0
cups=> 0
eggs=> 383
flour=> 0
happy_customer=> 660
money=> 20
muffin=> 0
packaged_coffee=> 0

======================================
Level 1:
--------------------------------------
Nice file! 13 processes, 12 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 241
Stock :
box_pastries=> 0
butter=> 28
coffee=> 0
coffee_beans=> 0
croissant=> 0
cups=> 85
eggs=> 380
flour=> 0
happy_customer=> 799
money=> 28
muffin=> 0
packaged_coffee=> 0

======================================
Level 2:
--------------------------------------
Nice file! 13 processes, 12 stocks, 2 to optimize
Evaluating .................. done.
no more process doable at time 281
Stock :
box_pastries=> 0
butter=> 90
coffee=> 105
coffee_beans=> 0
croissant=> 0
cups=> 0
eggs=> 383
flour=> 0
happy_customer=> 660
money=> 20
muffin=> 0
packaged_coffee=> 0

```


## Scenario: solar_system (maxDelay=250)

```
======================================
Level 0:
--------------------------------------
Nice file! 6 processes, 5 stocks, 2 to optimize
Evaluating .................. done.
Reached delay 250
Stock :
energy=> 2832
installed_panels=> 463
maintenance_kit=> 5
research=> 27
sun_rays=> 1009904

======================================
Level 1:
--------------------------------------
Nice file! 6 processes, 5 stocks, 2 to optimize
Evaluating .................. done.
Reached delay 250
Stock :
energy=> 520
installed_panels=> 10
maintenance_kit=> 5
sun_rays=> 1030700

======================================
Level 2:
--------------------------------------
Nice file! 6 processes, 5 stocks, 2 to optimize
Evaluating .................. done.
Reached delay 250
Stock :
energy=> 2832
installed_panels=> 463
maintenance_kit=> 5
research=> 27
sun_rays=> 1009904

```


## Scenario: test_relevance (maxDelay=100)

```
======================================
Level 0:
--------------------------------------
Nice file! 5 processes, 7 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 100
Stock :
equipment=> 0
euro=> 4
gold=> 10
happy_client=> 2
product=> 0

======================================
Level 1:
--------------------------------------
Nice file! 5 processes, 7 stocks, 1 to optimize
Evaluating .................. done.
Reached delay 100
Stock :
equipment=> 0
euro=> 4
gold=> 10
happy_client=> 2
product=> 0

======================================
Level 2:
--------------------------------------
Nice file! 5 processes, 7 stocks, 1 to optimize
Evaluating .................. done.
no more process doable at time 20
Stock :
equipment=> 0
euro=> 4
gold=> 0
happy_client=> 2
jewelry=> 0
luxury_item=> 2
product=> 0

```


---

## Test completed

Results saved to: OPTIMIZATION_COMPARISON_RESULTS.md
